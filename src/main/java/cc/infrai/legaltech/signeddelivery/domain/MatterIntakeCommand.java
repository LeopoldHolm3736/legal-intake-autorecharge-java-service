package cc.infrai.legaltech.signeddelivery.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MatterIntakeCommand(
        String matterId,
        String clientName,
        String clientEmail,
        String matterTitle,
        String signedDocumentUrl,
        LocalDate filingDeadline,
        BigDecimal minimumBalance,
        BigDecimal rechargeAmount
) {
}
