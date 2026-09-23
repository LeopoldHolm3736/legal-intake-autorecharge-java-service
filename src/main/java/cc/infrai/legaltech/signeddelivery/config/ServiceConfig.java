package cc.infrai.legaltech.signeddelivery.config;

import cc.infrai.legaltech.signeddelivery.application.MatterIntakeService;
import cc.infrai.legaltech.signeddelivery.infra.InfraiClient;
import cc.infrai.legaltech.signeddelivery.infra.LegalNoticeGateway;
import cc.infrai.legaltech.signeddelivery.infra.BalanceProtectionGateway;

public final class ServiceConfig {
    private final String apiKey;

    private ServiceConfig(String apiKey) {
        this.apiKey = apiKey;
    }

    public static ServiceConfig fromEnv() {
        String apiKey = System.getenv("INFRAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY is required");
        }
        return new ServiceConfig(apiKey);
    }

    public MatterIntakeService matterIntakeService() {
        InfraiClient infrai = new InfraiClient(apiKey);
        return new MatterIntakeService(
                new BalanceProtectionGateway(infrai),
                new LegalNoticeGateway(infrai)
        );
    }
}
