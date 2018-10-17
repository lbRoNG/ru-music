package com.lbrong.rumusic.common.net.exception;


public class BaseException extends Throwable {

    private int code;
    private String displayMessage;
    private Object data;

    BaseException(){}

    BaseException(int code,String displayMessage){
        this.code = code;
        this.displayMessage = displayMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
