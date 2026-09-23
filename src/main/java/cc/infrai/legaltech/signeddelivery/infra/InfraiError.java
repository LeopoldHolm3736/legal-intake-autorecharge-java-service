package cc.infrai.legaltech.signeddelivery.infra;

public final class InfraiError extends RuntimeException {
    private final int status;
    private final String code;

    public InfraiError(int status, String code, String message) {
        super(message == null ? code : message);
        this.status = status;
        this.code = code;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
