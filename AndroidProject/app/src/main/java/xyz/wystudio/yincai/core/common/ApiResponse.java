package xyz.wystudio.yincai.core.common;

import com.google.gson.annotations.SerializedName;

/** 后端统一 R<T> 响应。 */
public class ApiResponse<T> {
    @SerializedName("code")
    private String code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private T data;

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public boolean isSuccess() { return "SUCCESS".equalsIgnoreCase(code); }
}
