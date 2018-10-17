package com.lbrong.rumusic.common.net.exception;

import android.accounts.NetworkErrorException;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;

/**
 * 全局请求错误捕获
 */
public class ErrorHandler {

    public BaseException handlerError(Throwable e) {
        BaseException baseException = new BaseException();
        if (e instanceof HttpException) {
            baseException.setCode(ExceptionConstant.ERROR_HTTP);
        } else if (e instanceof SocketException) {
            baseException.setCode(ExceptionConstant.ERROR_NOT_NETWORK);
        } else if (e instanceof SocketTimeoutException) {
            baseException.setCode(ExceptionConstant.ERROR_SOCKET_TIMEOUT);
        } else if (e instanceof JSONException) {
            baseException.setCode(ExceptionConstant.ERROR_JSON);
        }else if (e instanceof JsonIOException) {
            baseException.setCode(ExceptionConstant.ERROR_JSON);
        } else if (e instanceof JsonSyntaxException) {
            baseException.setCode(ExceptionConstant.ERROR_JSON);
        } else if(e instanceof UnknownHostException) {
            baseException.setCode(ExceptionConstant.ERROR_NOT_NETWORK);
        } else if(e instanceof NetworkErrorException){
            baseException.setCode(ExceptionConstant.ERROR_NOT_NETWORK);
        } else if(e instanceof NetworkOnMainThreadException){
            baseException.setCode(ExceptionConstant.ERROR_CODE);
        }else {
            baseException.setCode(ExceptionConstant.ERROR_UNKNOW);
        }

        if(TextUtils.isEmpty(baseException.getDisplayMessage())){
            baseException.setDisplayMessage(ErrorMessageFactory.create(baseException.getCode()));
        }

        return baseException;
    }

}
