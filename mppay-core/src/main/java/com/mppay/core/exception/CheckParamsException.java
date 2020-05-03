package com.mppay.core.exception;

public class CheckParamsException extends RuntimeException{

    private String msg;

    public CheckParamsException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
