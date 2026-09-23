package cc.infrai.legaltech.signeddelivery.domain;

import java.time.LocalDate;

public record MatterIntakeResult(
        String matterId,
        ContinuityAction continuityAction,
        LocalDate followUpDate,
        String emailMessageId
) {
}
