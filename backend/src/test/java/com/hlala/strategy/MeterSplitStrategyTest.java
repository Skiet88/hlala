package com.hlala.strategy;

import com.hlala.domain.Enums;
import com.hlala.domain.Lease;
import com.hlala.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeterSplitStrategyTest {

    private Lease lease() {
        return new Lease(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("1500"), LocalDate.now(), null, Enums.LeaseLanguage.EN,
                Enums.SignatureStatus.UNSIGNED, Enums.SignatureStatus.UNSIGNED);
    }

    private static BigDecimal sum(Map<UUID, BigDecimal> shares) {
        return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void equalSplitAlwaysAddsUpToTheTopUp() {
        List<Lease> three = List.of(lease(), lease(), lease());
        Map<UUID, BigDecimal> shares = new EqualPerRoomSplitStrategy().split(new BigDecimal("100.00"), three, Map.of());

        assertThat(sum(shares)).isEqualByComparingTo("100.00");
        assertThat(shares.get(three.get(1).getId())).isEqualByComparingTo("33.33");
        assertThat(shares.get(three.get(0).getId())).isEqualByComparingTo("33.34"); // remainder cent
    }

    @Test
    void perPersonWeightsByHeadcount() {
        Lease couple = lease();
        Lease single = lease();
        Map<UUID, BigDecimal> shares = new PerPersonSplitStrategy().split(new BigDecimal("300"),
                List.of(couple, single), Map.of(couple.getId(), 2));

        assertThat(shares.get(couple.getId())).isEqualByComparingTo("200");
        assertThat(shares.get(single.getId())).isEqualByComparingTo("100");
        assertThat(sum(shares)).isEqualByComparingTo("300");
    }

    @Test
    void perPersonRejectsZeroOccupants() {
        Lease l = lease();
        assertThatThrownBy(() -> new PerPersonSplitStrategy().split(BigDecimal.TEN, List.of(l), Map.of(l.getId(), 0)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void cannotSplitWithNobodyLivingThere() {
        assertThatThrownBy(() -> new EqualPerRoomSplitStrategy().split(BigDecimal.TEN, List.of(), Map.of()))
                .isInstanceOf(ValidationException.class);
    }
}
