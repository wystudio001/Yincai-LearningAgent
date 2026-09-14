package xyz.wystudio.yincai.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或 Token 失效"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突或重复提交"),
    RATE_LIMITED(429, "请求过于频繁"),
    VALIDATION_FAILED(422, "参数校验失败"),
    AI_SERVICE_UNAVAILABLE(503, "AI 服务不可用"),
    RESOURCE_GENERATION_FAILED(500, "资源生成失败"),
    INTERNAL_ERROR(500, "系统内部错误");

    private final int httpStatus;
    private final String defaultMessage;
}
