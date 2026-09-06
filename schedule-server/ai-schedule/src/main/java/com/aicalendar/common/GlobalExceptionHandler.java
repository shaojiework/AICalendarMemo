package com.aicalendar.common;

import com.aicalendar.dto.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Result<Map<String, String>> result = new Result<>();
        result.setCode(ResponseCode.PARAM_ERROR.getCode());
        result.setMessage("参数校验失败");
        result.setData(errors);

        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理业务异常（登录失败、权限不足等）
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException ex) {
        return ResponseEntity.status(ex.getCode())
                .body(Result.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理日期解析异常（如路径参数日期格式非法）
     */
    @ExceptionHandler(java.time.format.DateTimeParseException.class)
    public ResponseEntity<Result<Void>> handleDateTimeParseException(java.time.format.DateTimeParseException ex) {
        return ResponseEntity.badRequest()
                .body(Result.error(ResponseCode.PARAM_ERROR.getCode(), "日期格式错误，应为yyyy-MM-dd"));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Result<Void> result = new Result<>();
        result.setCode(ResponseCode.PARAM_ERROR.getCode());
        result.setMessage(ex.getMessage());

        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        Result<Void> result = new Result<>();
        result.setCode(ResponseCode.ERROR.getCode());
        result.setMessage("服务器内部错误");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
