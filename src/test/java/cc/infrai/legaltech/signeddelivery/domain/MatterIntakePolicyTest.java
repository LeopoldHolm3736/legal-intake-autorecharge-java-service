package cc.infrai.legaltech.signeddelivery.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatterIntakePolicyTest {
    @Test
    void triggersTopupAndComputesFollowUpDateWhenBalanceIsUnderThreshold() {
        BalanceDecision decision = MatterIntakePolicy.decide(
                new BigDecimal("12.00"),
                new BigDecimal("25.00"),
                new BigDecimal("75.00"),
                new BigDecimal("25.00")
        );

        assertEquals(ContinuityAction.TRIGGERED_TOPUP, decision.action());
        assertEquals(LocalDate.of(2026, 2, 8), MatterIntakePolicy.followUpDate(LocalDate.of(2026, 2, 15)));
    }
}
