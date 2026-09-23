package cc.infrai.legaltech.signeddelivery.application;

import cc.infrai.legaltech.signeddelivery.domain.BalanceDecision;
import cc.infrai.legaltech.signeddelivery.domain.ContinuityAction;
import cc.infrai.legaltech.signeddelivery.domain.MatterIntakeCommand;
import cc.infrai.legaltech.signeddelivery.domain.MatterIntakePolicy;
import cc.infrai.legaltech.signeddelivery.domain.MatterIntakeResult;
import cc.infrai.legaltech.signeddelivery.infra.BalanceProtectionGateway;
import cc.infrai.legaltech.signeddelivery.infra.LegalNoticeGateway;

public final class MatterIntakeService {
    private final BalanceProtectionGateway balanceProtectionGateway;
    private final LegalNoticeGateway legalNoticeGateway;

    public MatterIntakeService(BalanceProtectionGateway balanceProtectionGateway,
                               LegalNoticeGateway legalNoticeGateway) {
        this.balanceProtectionGateway = balanceProtectionGateway;
        this.legalNoticeGateway = legalNoticeGateway;
    }

    public MatterIntakeResult intake(MatterIntakeCommand command) throws Exception {
        BalanceDecision decision = balanceProtectionGateway.protect(command.minimumBalance(), command.rechargeAmount(), command.matterId());
        String messageId = legalNoticeGateway.sendSignedDocumentNotice(command, MatterIntakePolicy.followUpDate(command.filingDeadline()));
        ContinuityAction action = decision.action();
        return new MatterIntakeResult(
                command.matterId(),
                action,
                MatterIntakePolicy.followUpDate(command.filingDeadline()),
                messageId
        );
    }
}
