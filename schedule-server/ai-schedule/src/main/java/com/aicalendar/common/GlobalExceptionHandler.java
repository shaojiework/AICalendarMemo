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
     * 处理业务异常（登录失败、权限不足、令牌失效等）
     * 令牌类业务码为四位数字，不是合法HTTP状态码，统一映射为HTTP 401，
     * 真实原因通过返回体code区分，前端据此决定静默刷新还是跳登录页
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException ex) {
        return ResponseEntity.status(toHttpStatus(ex.getCode()))
                .body(Result.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 业务码转HTTP状态码：仅在合法区间内直接复用，越界时归到401/500
     */
    private int toHttpStatus(int code) {
        if (code >= 100 && code <= 599) {
            return code;
        }
        if (code >= 40100 && code < 40200) {
            return HttpStatus.UNAUTHORIZED.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
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
