package com.jkweilai.meinian.api.exception;

import lombok.Data;

@Data
public class HisException extends RuntimeException {
    private String msg;
    private int code = 500;

    // 捕获到其他异常时直接包装
    public HisException(Exception e) {
        super(e);
        this.msg = "执行异常";
    }
    // 简单的业务逻辑错误提示
    public HisException(String msg) {
        super(msg);
        this.msg = msg;
    }
    // 既有自定义错误信息，又要保留原始异常栈
    public HisException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }
    // 需要指定特定错误码的业务异常
    public HisException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }
    // 最完整的场景 - 既有错误码、自定义信息，又保留原始异常
    public HisException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}