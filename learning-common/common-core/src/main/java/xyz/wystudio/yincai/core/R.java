package xyz.wystudio.yincai.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private T data;
    private String traceId;
    private OffsetDateTime timestamp;

    // ---------- success ----------
    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return R.<T>builder()
                .code(ErrorCode.SUCCESS.name())
                .message("操作成功")
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // ---------- fail with ErrorCode ----------
    public static <T> R<T> fail(ErrorCode errorCode) {
        return R.<T>builder()
                .code(errorCode.name())
                .message(errorCode.getDefaultMessage())
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        return R.<T>builder()
                .code(errorCode.name())
                .message(message)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // ---------- pagination ----------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageData<T> implements Serializable {
        private java.util.List<T> records;
        private long pageNo;
        private long pageSize;
        private long total;
        private boolean hasNext;
    }

    public static <T> R<PageData<T>> page(PageData<T> pageData) {
        return ok(pageData);
    }
}
