package com.lbrong.rumusic.common.net.exception;

public class ExceptionConstant {

    //---------------- Http 错误定义 ----------------

    // Http 错误
    public static final int ERROR_HTTP = 1002;
    // 请求错误
    public static final int ERROR_HTTP_400 = 400;
    // 请求未授权
    public static final int ERROR_HTTP_401 = 401;
    // 请求资源未找到
    public static final int ERROR_HTTP_404 = 404;
    // 服务器内部错误
    public static final int ERROR_HTTP_500 = 500;


    //---------------- 其他错误定义 ----------------

    // 连接超时
    public static final int ERROR_SOCKET_TIMEOUT = 1003;
    // 无网络
    public static final int ERROR_NOT_NETWORK = 1004;
    // Json 解析错误
    public static final int ERROR_JSON = 1005;
    // 未知错误
    public static final int ERROR_UNKNOW = 1006;
    // 代码问题
    public static final int ERROR_CODE = 1007;
}
