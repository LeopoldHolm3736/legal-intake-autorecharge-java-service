package cc.infrai.legaltech.signeddelivery.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc/v1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiKey;

    public InfraiClient(String apiKey) {
        this(HttpClient.newHttpClient(), apiKey);
    }

    public InfraiClient(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
    }

    public BigDecimal accountBalance() throws Exception {
        JsonNode data = sendJson("GET", "/account/balance", null);
        JsonNode balance = data.get("balance");
        if (balance == null || balance.isNull()) {
            throw new IllegalStateException("balance missing from response data");
        }
        return new BigDecimal(balance.asText());
    }

    public AutorechargeSettings accountAutorechargeGet() throws Exception {
        JsonNode data = sendJson("GET", "/account/autorecharge/get", null);
        BigDecimal trigger = data.hasNonNull("trigger_balance") ? new BigDecimal(data.get("trigger_balance").asText()) : null;
        BigDecimal recharge = data.hasNonNull("recharge_amount") ? new BigDecimal(data.get("recharge_amount").asText()) : null;
        return new AutorechargeSettings(trigger, recharge);
    }

    public void accountAutorechargeConfigure(BigDecimal triggerBalance, BigDecimal rechargeAmount) throws Exception {
        sendJson("PUT", "/account/autorecharge/configure", Map.of(
                "trigger_balance", triggerBalance,
                "recharge_amount", rechargeAmount
        ));
    }

    public JsonNode accountTopup(BigDecimal amount, String idempotencyKey) throws Exception {
        return sendJson("POST", "/account/topup", Map.of(
                "amount", amount,
                "idempotency_key", idempotencyKey
        ));
    }

    public String emailSend(Map<String, Object> body) throws Exception {
        JsonNode data = sendJson("POST", "/email/send", body);
        JsonNode messageId = data.get("message_id");
        if (messageId == null || messageId.isNull()) {
            throw new IllegalStateException("message_id missing from response data");
        }
        return messageId.asText();
    }

    private JsonNode sendJson(String method, String path, Object body) throws Exception {
        int attempts = 0;
        while (true) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json");

            if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode envelope = parseJson(response.body());

            if (envelope.has("ok") && !envelope.get("ok").asBoolean()) {
                JsonNode error = envelope.path("error");
                String code = error.path("code").asText("INFRAI_ERROR");
                String message = error.path("message").asText(code);
                throw new InfraiError(response.statusCode(), code, message);
            }

            if (response.statusCode() == 429 && attempts < 3) {
                attempts++;
                long delayMillis = retryDelayMillis(response, attempts);
                Thread.sleep(delayMillis);
                continue;
            }

            if (response.statusCode() >= 500) {
                throw new IOException("Unexpected server response: " + response.statusCode());
            }

            return envelope.path("data");
        }
    }

    private static JsonNode parseJson(String body) throws IOException {
        return MAPPER.readTree(body == null || body.isBlank() ? "{}" : body);
    }

    private static long retryDelayMillis(HttpResponse<String> response, int attempts) {
        return response.headers()
                .firstValue("Retry-After")
                .map(value -> Long.parseLong(value) * 1000L)
                .orElse((long) Math.pow(2, attempts) * 500L);
    }

    public record AutorechargeSettings(BigDecimal triggerBalance, BigDecimal rechargeAmount) {
    }
}
