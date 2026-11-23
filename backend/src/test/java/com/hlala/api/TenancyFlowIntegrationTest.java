package com.hlala.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlala.config.DevHeaderAuthenticationFilter;
import com.hlala.domain.AdminProfile;
import com.hlala.domain.Enums;
import com.hlala.repository.AdminProfileRepository;
import com.hlala.repository.CheckerProfileRepository;
import com.hlala.repository.LeaseRepository;
import com.hlala.domain.Lease;
import com.hlala.service.Actor;
import com.hlala.service.EscrowService;
import com.hlala.support.PostgresContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives the whole system over HTTP, the way the app will: list a room, get
 * it checked, apply, pay into the hold, move in, pay rent, hand back keys,
 * and release the deposit with two admins. Also proves the dispute path and
 * the access rules. Uses the local profile's dev header for identity.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresContainerConfig.class)
class TenancyFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AdminProfileRepository admins;
    @Autowired CheckerProfileRepository checkers;
    @Autowired EscrowService escrow;
    @Autowired LeaseRepository leaseRows;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    private UUID adminA;
    private UUID adminB;
    private UUID checker;

    @BeforeEach
    void seededStaff() {
        List<AdminProfile> all = admins.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        adminA = all.get(0).getUserId();
        adminB = all.get(1).getUserId();
        checker = checkers.findAll().get(0).getUserId();
    }

    // ---------------------------------------------------------------- tests

    @Test
    void fullTenancyReleasesDepositOnlyAfterTwoAdmins() throws Exception {
        Tenancy t = tenancyThroughMoveIn(true);

        // Rent while living there.
        call(t.landlord, post("/leases/" + t.leaseId + "/rent-payments"),
                "{\"amount\":1500,\"method\":\"CASH\",\"coveredPeriod\":\"" + YearMonth.now() + "\"}")
                .andExpect(status().isCreated());
        JsonNode book = read(call(t.tenant, get("/leases/" + t.leaseId + "/rent-book"), null).andExpect(status().isOk()));
        assertThat(book.get("collected").decimalValue()).isEqualByComparingTo("1500");

        // Move out: tenant hands back keys. Money still does not move by itself.
        call(t.tenant, post("/deposit-holds/" + t.holdId + "/confirm-handover"), null).andExpect(status().isOk());

        JsonNode afterFirst = read(call(adminA, post("/deposit-holds/" + t.holdId + "/approvals"), null)
                .andExpect(status().isCreated()));
        assertThat(afterFirst.at("/hold/state").asText()).isEqualTo("ACTIVE");

        // Same admin again: refused.
        call(adminA, post("/deposit-holds/" + t.holdId + "/approvals"), null).andExpect(status().isBadRequest());

        JsonNode afterSecond = read(call(adminB, post("/deposit-holds/" + t.holdId + "/approvals"), null)
                .andExpect(status().isCreated()));
        assertThat(afterSecond.at("/hold/state").asText()).isEqualTo("RELEASED");

        // Tenant sees state only - never the audit trail.
        JsonNode tenantView = read(call(t.tenant, get("/deposit-holds/" + t.holdId), null).andExpect(status().isOk()));
        assertThat(tenantView.get("state").asText()).isEqualTo("RELEASED");
        assertThat(tenantView.has("history")).isFalse();

        // Sealed reviews: hidden from the other side until both have posted.
        call(t.tenant, post("/leases/" + t.leaseId + "/reviews"), "{\"stars\":5,\"tags\":[\"Paid on time\"]}")
                .andExpect(status().isCreated());
        JsonNode landlordSees = read(call(t.landlord, get("/leases/" + t.leaseId + "/reviews"), null));
        assertThat(landlordSees).isEmpty();
        call(t.landlord, post("/leases/" + t.leaseId + "/reviews"), "{\"stars\":4}").andExpect(status().isCreated());
        JsonNode tenantSees = read(call(t.tenant, get("/leases/" + t.leaseId + "/reviews"), null));
        assertThat(tenantSees).hasSize(2);
    }

    @Test
    void disputeFreezesMoneyUntilResolvedAndApprovedByTwoAdmins() throws Exception {
        Tenancy t = tenancyThroughMoveIn(false);

        JsonNode dispute = read(call(t.tenant, post("/disputes"),
                "{\"depositHoldId\":\"" + t.holdId + "\",\"evidence\":[\"voice://note-1\"]}")
                .andExpect(status().isCreated()));
        String disputeId = dispute.get("id").asText();
        assertThat(state(t)).isEqualTo("DISPUTED");

        // Nothing to approve while the dispute is open.
        call(adminA, post("/deposit-holds/" + t.holdId + "/approvals"), null).andExpect(status().isConflict());

        // A party cannot resolve their own dispute.
        call(t.tenant, post("/disputes/" + disputeId + "/resolve"), "{\"resolution\":\"TENANT\"}")
                .andExpect(status().isForbidden());

        call(adminA, post("/disputes/" + disputeId + "/resolve"), "{\"resolution\":\"TENANT\"}")
                .andExpect(status().isOk());
        assertThat(state(t)).isEqualTo("DISPUTED"); // decided, but money waits

        call(adminA, post("/deposit-holds/" + t.holdId + "/approvals"), null).andExpect(status().isCreated());
        assertThat(state(t)).isEqualTo("DISPUTED");
        call(adminB, post("/deposit-holds/" + t.holdId + "/approvals"), null).andExpect(status().isCreated());
        assertThat(state(t)).isEqualTo("REFUNDED");

        JsonNode audit = read(call(adminA, get("/admin/deposit-holds/" + t.holdId), null).andExpect(status().isOk()));
        assertThat(audit.get("history").size()).isGreaterThanOrEqualTo(5);
    }

    /**
     * Two admins approving the same payout at the same moment. Without a lock
     * on the hold, each transaction sees only its own approval, neither
     * releases the money, and both are then refused as "already approved".
     * With the lock, one of them conflicts, retries, sees both approvals and
     * releases. Repeated because a race may not show on a single attempt.
     */
    @Test
    void simultaneousApprovalsStillReleaseTheDeposit() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 40; i++) {
                Tenancy t = tenancyThroughMoveIn(false);
                call(t.tenant, post("/deposit-holds/" + t.holdId + "/confirm-handover"), null)
                        .andExpect(status().isOk());
                UUID holdId = UUID.fromString(t.holdId);
                CyclicBarrier together = new CyclicBarrier(2);
                Future<?> a = pool.submit(() -> approveWithOneRetry(adminA, holdId, together));
                Future<?> b = pool.submit(() -> approveWithOneRetry(adminB, holdId, together));
                a.get();
                b.get();
                assertThat(state(t)).as("attempt %d", i).isEqualTo("RELEASED");
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Tenant and landlord sign from the same starting copy of the lease. The
     * second save is refused rather than silently wiping the first signature.
     */
    @Test
    void staleCopyCannotOverwriteANewerLease() throws Exception {
        Tenancy t = tenancyThroughMoveIn(false);
        UUID leaseId = UUID.fromString(t.leaseId);
        Lease seenByTenant = leaseRows.findById(leaseId).orElseThrow();
        Lease seenByLandlord = leaseRows.findById(leaseId).orElseThrow();

        leaseRows.save(seenByTenant.withTenantSignature(Enums.SignatureStatus.SIGNED));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        leaseRows.save(seenByLandlord.withLandlordSignature(Enums.SignatureStatus.SIGNED)))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(leaseRows.findById(leaseId).orElseThrow().getTenantSignature())
                .isEqualTo(Enums.SignatureStatus.SIGNED);
    }

    /**
     * Supabase exposes public tables through its Data API. Every table must
     * have row level security on (and no policies), or that API can reach it.
     */
    @Test
    void everyTableHasRowLevelSecurity() {
        List<String> open = jdbc.queryForList(
                "select tablename from pg_tables where schemaname = 'public' and not rowsecurity", String.class);
        assertThat(open).as("tables without row level security").isEmpty();
    }

    private Void approveWithOneRetry(UUID adminUserId, UUID holdId, CyclicBarrier together) throws Exception {
        Actor admin = new Actor(adminUserId, Set.of(Enums.Role.ADMIN));
        together.await();
        try {
            escrow.approve(admin, holdId);
        } catch (OptimisticLockingFailureException conflict) {
            escrow.approve(admin, holdId); // what the client does on a 409
        }
        return null;
    }

    @Test
    void accessRulesHold() throws Exception {
        Tenancy t = tenancyThroughMoveIn(false);
        UUID stranger = registerAs("TENANT");

        mvc.perform(get("/tenants/me/home")).andExpect(status().isUnauthorized());
        call(t.tenant, get("/admin/money-trail"), null).andExpect(status().isForbidden());
        call(t.tenant, post("/deposit-holds/" + t.holdId + "/approvals"), null).andExpect(status().isForbidden());
        call(checker, post("/rooms"), roomJson()).andExpect(status().isForbidden());
        // Right role, wrong person: ownership is enforced in the service layer.
        call(stranger, get("/deposit-holds/" + t.holdId), null).andExpect(status().isForbidden());
        call(stranger, get("/leases/" + t.leaseId + "/rent-book"), null).andExpect(status().isForbidden());
        // Admin and checker cannot be self-assigned.
        call(stranger, post("/accounts/me/roles"), "{\"role\":\"ADMIN\"}").andExpect(status().isForbidden());
    }

    @Test
    void publicCanBrowseButNotSeeExactPin() throws Exception {
        Tenancy t = tenancyThroughMoveIn(false);
        // After move-in the room is LET, so make a fresh live listing to browse.
        UUID landlord = registerAs("LANDLORD");
        String roomId = read(call(landlord, post("/rooms"), roomJson()).andExpect(status().isCreated())).get("id").asText();
        String listingId = read(call(landlord, post("/rooms/" + roomId + "/listings"),
                "{\"rentAmount\":1200,\"depositAmount\":1200}")).get("id").asText();
        call(landlord, post("/listings/" + listingId + "/publish"), "{\"requestCheck\":false}").andExpect(status().isOk());

        JsonNode detail = read(mvc.perform(get("/listings/" + listingId)).andExpect(status().isOk()));
        assertThat(detail.at("/room/area").asText()).contains("Khayelitsha");
        assertThat(detail.at("/room").has("pin")).isFalse();
        assertThat(t.holdId).isNotNull();
    }

    // ------------------------------------------------------------ helpers

    private record Tenancy(UUID landlord, UUID tenant, String listingId, String leaseId, String holdId) {
    }

    /** Landlord lists, checker verifies, tenant applies and is accepted, pays, moves in. */
    private Tenancy tenancyThroughMoveIn(boolean verify) throws Exception {
        UUID landlord = registerAs("LANDLORD");
        UUID tenant = registerAs("TENANT");

        String roomId = read(call(landlord, post("/rooms"), roomJson()).andExpect(status().isCreated())).get("id").asText();
        String listingId = read(call(landlord, post("/rooms/" + roomId + "/listings"),
                "{\"rentAmount\":1500,\"depositAmount\":1500}").andExpect(status().isCreated())).get("id").asText();
        call(landlord, post("/listings/" + listingId + "/publish"), "{\"requestCheck\":" + verify + "}")
                .andExpect(status().isOk());

        if (verify) {
            String check = "{\"listingId\":\"" + listingId + "\",\"results\":["
                    + "{\"item\":\"Room exists\",\"passed\":true},{\"item\":\"Landlord ID\",\"passed\":true},"
                    + "{\"item\":\"Toilet\",\"passed\":true},{\"item\":\"Meter\",\"passed\":true},"
                    + "{\"item\":\"Locks\",\"passed\":true}],"
                    + "\"photos\":[\"p://1\",\"p://2\",\"p://3\",\"p://4\",\"p://5\",\"p://6\"],"
                    + "\"checkedAt\":{\"latitude\":-34.0383,\"longitude\":18.6778}}";
            JsonNode result = read(call(checker, post("/room-checks"), check).andExpect(status().isCreated()));
            assertThat(result.get("passed").asBoolean()).isTrue();
            JsonNode verified = read(mvc.perform(get("/listings").param("verifiedOnly", "true")));
            assertThat(verified.findValuesAsText("id")).contains(listingId);
        }

        String appId = read(call(tenant, post("/listings/" + listingId + "/applications"), null)
                .andExpect(status().isCreated())).get("id").asText();
        call(landlord, post("/applications/" + appId + "/decision"), "{\"accept\":true}").andExpect(status().isOk());

        JsonNode started = read(call(tenant, post("/applications/" + appId + "/tenancy"),
                "{\"startDate\":\"" + LocalDate.now() + "\",\"language\":\"XH\"}").andExpect(status().isCreated()));
        String leaseId = started.at("/lease/id").asText();
        String holdId = started.at("/hold/id").asText();
        assertThat(started.at("/hold/state").asText()).isEqualTo("PENDING");

        call(adminA, post("/deposit-holds/" + holdId + "/payment-confirmed"), null).andExpect(status().isOk());
        JsonNode moved = read(call(tenant, post("/deposit-holds/" + holdId + "/confirm-moved-in"), null)
                .andExpect(status().isOk()));
        assertThat(moved.get("state").asText()).isEqualTo("ACTIVE");

        return new Tenancy(landlord, tenant, listingId, leaseId, holdId);
    }

    private String state(Tenancy t) throws Exception {
        return read(call(t.tenant, get("/deposit-holds/" + t.holdId), null)).get("state").asText();
    }

    private UUID registerAs(String role) throws Exception {
        String phone = "+2782" + ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999);
        JsonNode user = read(mvc.perform(post("/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"name\":\"Test " + role + "\"}"))
                .andExpect(status().isCreated()));
        UUID id = UUID.fromString(user.get("id").asText());
        call(id, post("/accounts/me/roles"), "{\"role\":\"" + role + "\"}").andExpect(status().isOk());
        return id;
    }

    private static String roomJson() {
        return "{\"area\":\"Khayelitsha Site C\",\"landmarkNote\":\"Behind the spaza near the taxi rank\","
                + "\"pin\":{\"latitude\":-34.0383,\"longitude\":18.6778},\"type\":\"BACKYARD_ROOM\","
                + "\"amenities\":[\"PRIVATE_ENTRANCE\",\"ELECTRICITY_PREPAID\"],\"occupancyMode\":\"SOLE\"}";
    }

    private ResultActions call(UUID user, MockHttpServletRequestBuilder req, String body) throws Exception {
        req.header(DevHeaderAuthenticationFilter.HEADER, user.toString());
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(req);
    }

    private JsonNode read(ResultActions result) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        return body.isEmpty() ? json.createObjectNode() : json.readTree(body);
    }
}
