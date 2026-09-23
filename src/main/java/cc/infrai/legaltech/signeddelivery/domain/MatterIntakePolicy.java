package cc.infrai.legaltech.signeddelivery.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class MatterIntakePolicy {
    private MatterIntakePolicy() {
    }

    public static BalanceDecision decide(BigDecimal currentBalance,
                                         BigDecimal requestedTrigger,
                                         BigDecimal rechargeAmount,
                                         BigDecimal configuredTrigger) {
        if (configuredTrigger == null || configuredTrigger.compareTo(requestedTrigger) != 0) {
            return new BalanceDecision(ContinuityAction.CONFIGURED_AUTORECHARGE);
        }
        if (currentBalance.compareTo(requestedTrigger) < 0 && rechargeAmount.signum() > 0) {
            return new BalanceDecision(ContinuityAction.TRIGGERED_TOPUP);
        }
        return new BalanceDecision(ContinuityAction.NO_ACTION);
    }

    public static LocalDate followUpDate(LocalDate filingDeadline) {
        return filingDeadline.minusDays(7);
    }
}
