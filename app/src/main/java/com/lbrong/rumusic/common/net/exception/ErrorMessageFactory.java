package com.lbrong.rumusic.common.net.exception;


public class ErrorMessageFactory {

    public static String create(int code) {
        String errorMsg;
        switch (code) {
            case ExceptionConstant.ERROR_NOT_NETWORK:
                errorMsg = "当前网络不可用，请检查网络设置";
                break;
            case ExceptionConstant.ERROR_SOCKET_TIMEOUT:
                errorMsg = "网络连接超时，请检查网络设置";
                break;
            case ExceptionConstant.ERROR_HTTP_400:
            case ExceptionConstant.ERROR_HTTP_404:
            case ExceptionConstant.ERROR_HTTP_401:
            case ExceptionConstant.ERROR_HTTP:
            case ExceptionConstant.ERROR_HTTP_500:
                errorMsg = "服务器开了个小差,请稍后重试";
                break;
            case ExceptionConstant.ERROR_JSON:
                errorMsg = "解析出错";
                break;
            case ExceptionConstant.ERROR_CODE:
                errorMsg = "代码出错";
                break;
            default:
                errorMsg = "未知错误";
                break;
        }
        return errorMsg;
    }

}
