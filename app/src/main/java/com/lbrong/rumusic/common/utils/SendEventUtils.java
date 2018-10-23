package com.lbrong.rumusic.common.utils;

import com.jeremyliao.livedatabus.LiveDataBus;

/**
 * @author lbRoNG
 * @since 2018/10/23
 */
public final class SendEventUtils {
    private SendEventUtils(){}

    public static <T> void sendForMain(String key,T value){
        LiveDataBus.get().with(key).setValue(value);
    }

    public static <T> void sendForBack(String key,T value){
        LiveDataBus.get().with(key).postValue(value);
    }

    public static <T> LiveDataBus.Observable<T> observe(String key, Class<T> type){
        return (LiveDataBus.Observable<T>) LiveDataBus.get().with(key, type.getClass());
    }

}
