package com.aicalendar.common;

/**
 * 业务异常：携带业务码，由全局异常处理器统一转换为Result响应
 */
public class BizException extends RuntimeException {

    /** 业务码 */
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
