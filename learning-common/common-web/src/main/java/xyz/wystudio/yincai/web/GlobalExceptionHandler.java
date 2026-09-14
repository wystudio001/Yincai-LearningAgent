package xyz.wystudio.yincai.web;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.core.R;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException ex) {
        log.warn("BizException: code={}, message={}", ex.getErrorCode().name(), ex.getMessage());
        R<Void> body = R.fail(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        R<Void> body = R.fail(ErrorCode.VALIDATION_FAILED, msg);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleBind(BindException ex) {
        String msg = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        R<Void> body = R.fail(ErrorCode.VALIDATION_FAILED, msg);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        R<Void> body = R.fail(ErrorCode.VALIDATION_FAILED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Request body parse failed: {}", ex.getMessage());
        R<Void> body = R.fail(ErrorCode.BAD_REQUEST, "请求体格式错误");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        R<Void> body = R.fail(ErrorCode.INTERNAL_ERROR, "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
