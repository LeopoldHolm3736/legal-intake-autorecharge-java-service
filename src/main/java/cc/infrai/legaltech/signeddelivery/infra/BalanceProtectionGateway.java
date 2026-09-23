package cc.infrai.legaltech.signeddelivery.infra;

import cc.infrai.legaltech.signeddelivery.domain.BalanceDecision;
import cc.infrai.legaltech.signeddelivery.domain.ContinuityAction;
import cc.infrai.legaltech.signeddelivery.domain.MatterIntakePolicy;

import java.math.BigDecimal;

public final class BalanceProtectionGateway {
    private final InfraiClient infrai;

    public BalanceProtectionGateway(InfraiClient infrai) {
        this.infrai = infrai;
    }

    public BalanceDecision protect(BigDecimal minimumBalance, BigDecimal rechargeAmount, String matterId) throws Exception {
        BigDecimal currentBalance = infrai.accountBalance();
        InfraiClient.AutorechargeSettings settings = infrai.accountAutorechargeGet();
        BalanceDecision decision = MatterIntakePolicy.decide(currentBalance, minimumBalance, rechargeAmount, settings.triggerBalance());

        if (decision.action() == ContinuityAction.CONFIGURED_AUTORECHARGE) {
            infrai.accountAutorechargeConfigure(minimumBalance, rechargeAmount);
            if (currentBalance.compareTo(minimumBalance) < 0) {
                infrai.accountTopup(rechargeAmount, "matter-" + matterId + "-topup");
                return new BalanceDecision(ContinuityAction.TRIGGERED_TOPUP);
            }
        }

        if (decision.action() == ContinuityAction.TRIGGERED_TOPUP) {
            infrai.accountTopup(rechargeAmount, "matter-" + matterId + "-topup");
        }

        return decision;
    }
}
