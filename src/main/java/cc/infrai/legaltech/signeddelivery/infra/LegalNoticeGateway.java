package cc.infrai.legaltech.signeddelivery.infra;

import cc.infrai.legaltech.signeddelivery.domain.MatterIntakeCommand;

import java.time.LocalDate;
import java.util.Map;

public final class LegalNoticeGateway {
    private final InfraiClient infrai;

    public LegalNoticeGateway(InfraiClient infrai) {
        this.infrai = infrai;
    }

    public String sendSignedDocumentNotice(MatterIntakeCommand command, LocalDate followUpDate) throws Exception {
        String text = "Hello " + command.clientName() + ",\n\n"
                + "Your signed document for matter '" + command.matterTitle() + "' is ready: " + command.signedDocumentUrl() + "\n"
                + "Please review it before " + command.filingDeadline() + ".\n"
                + "If we do not hear from you, we will follow up on " + followUpDate + ".\n";

        return infrai.emailSend(Map.of(
                "to", command.clientEmail(),
                "subject", "Signed document ready for " + command.matterTitle(),
                "body", text
        ));
    }
}
