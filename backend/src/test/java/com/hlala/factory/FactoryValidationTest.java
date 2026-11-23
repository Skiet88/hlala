package com.hlala.factory;

import com.hlala.domain.DepositHold;
import com.hlala.domain.Enums;
import com.hlala.domain.Lease;
import com.hlala.domain.Listing;
import com.hlala.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FactoryValidationTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void newListingStartsAsUnverifiedDraft() {
        Listing l = new ListingFactory().create(id, new BigDecimal("1500"), new BigDecimal("1500"));
        assertThat(l.getStatus()).isEqualTo(Enums.ListingStatus.DRAFT);
        assertThat(l.getVerificationStatus()).isEqualTo(Enums.VerificationStatus.UNVERIFIED);
    }

    @Test
    void listingRejectsZeroOrMissingMoney() {
        ListingFactory f = new ListingFactory();
        assertThatThrownBy(() -> f.create(id, BigDecimal.ZERO, BigDecimal.TEN)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> f.create(id, BigDecimal.TEN, null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void depositHoldAlwaysOpensPending() {
        DepositHold h = new DepositHoldFactory().open(id, new BigDecimal("1500"));
        assertThat(h.getState()).isEqualTo(Enums.DepositHoldState.PENDING);
        assertThat(h.getHandoverConfirmedAt()).isNull();
    }

    @Test
    void depositHoldRejectsNegativeAmount() {
        assertThatThrownBy(() -> new DepositHoldFactory().open(id, new BigDecimal("-1")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void leaseStartsUnsignedAndRejectsEndBeforeStart() {
        LeaseFactory f = new LeaseFactory();
        Lease lease = f.create(id, id, id, new BigDecimal("1500"), LocalDate.now(), null, Enums.LeaseLanguage.XH);
        assertThat(lease.isFullySigned()).isFalse();

        assertThatThrownBy(() -> f.create(id, id, id, new BigDecimal("1500"), LocalDate.now(),
                LocalDate.now().minusDays(1), Enums.LeaseLanguage.EN)).isInstanceOf(ValidationException.class);
    }

    @Test
    void userNeedsSouthAfricanMobileNumber() {
        UserFactory f = new UserFactory();
        assertThat(f.create("+27821234567", "Lwazi").isPhoneVerified()).isFalse();
        assertThatThrownBy(() -> f.create("0821234567", "Lwazi")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> f.create("+27821234567", " ")).isInstanceOf(ValidationException.class);
    }

    @Test
    void reviewStarsMustBeOneToFiveAndNotAboutYourself() {
        CareFactory f = new CareFactory();
        UUID other = UUID.randomUUID();
        assertThat(f.review(id, id, other, 5, Set.of("Clean"), null).isSealed()).isTrue();
        assertThatThrownBy(() -> f.review(id, id, other, 6, Set.of(), null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> f.review(id, id, id, 4, Set.of(), null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void reportNeedsWordsOrAttachment() {
        assertThatThrownBy(() -> new CareFactory().report(id, Enums.ReportReason.OTHER, " ", null))
                .isInstanceOf(ValidationException.class);
    }
}
