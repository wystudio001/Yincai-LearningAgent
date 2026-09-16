package xyz.wystudio.yincai.core.common;

public class ApiException extends Exception {
    private final int httpCode;
    private final String businessCode;

    public ApiException(String message) { this(0, null, message); }
    public ApiException(int httpCode, String businessCode, String message) {
        super(message);
        this.httpCode = httpCode;
        this.businessCode = businessCode;
    }
    public int getHttpCode() { return httpCode; }
    public String getBusinessCode() { return businessCode; }
}
