package cc.infrai.legaltech.signeddelivery;

import cc.infrai.legaltech.signeddelivery.application.MatterIntakeService;
import cc.infrai.legaltech.signeddelivery.config.ServiceConfig;
import cc.infrai.legaltech.signeddelivery.domain.MatterIntakeCommand;
import cc.infrai.legaltech.signeddelivery.domain.MatterIntakeResult;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class SignedDeliveryExample {
    public static void main(String[] args) throws Exception {
        ServiceConfig config = ServiceConfig.fromEnv();
        MatterIntakeService service = config.matterIntakeService();

        MatterIntakeCommand command = new MatterIntakeCommand(
                "M-2026-001",
                "Avery Cole",
                "chenhua@changba.com",
                "Cole v. North Harbor School",
                "https://files.example.com/signed/cole-settlement.pdf",
                LocalDate.of(2026, 2, 15),
                new BigDecimal("25.00"),
                new BigDecimal("75.00")
        );

        MatterIntakeResult result = service.intake(command);

        System.out.println("matterId=" + result.matterId());
        System.out.println("continuityAction=" + result.continuityAction());
        System.out.println("followUpDate=" + result.followUpDate());
        System.out.println("emailMessageId=" + result.emailMessageId());
    }
}
