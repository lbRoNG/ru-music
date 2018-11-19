package com.lbrong.rumusic.common.utils;

import org.greenrobot.eventbus.EventBus;

/**
 * @author lbRoNG
 * @since 2018/10/23
 */
public final class SendEventUtils {
    private SendEventUtils() {}

    public static void register(Object o) {
        if (!EventBus.getDefault().isRegistered(o)) {
            EventBus.getDefault().register(o);
        }
    }

    public static void unregister(Object o) {
        if (EventBus.getDefault().isRegistered(o)) {
            EventBus.getDefault().unregister(o);
        }
    }

    public static void post(Object o) {
        EventBus.getDefault().post(o);
    }

    public static void postSticky(Object o) {
        EventBus.getDefault().postSticky(o);
    }

    public static void removeStickyEvent(Object o) {
        EventBus.getDefault().removeStickyEvent(o);
    }

    public static void removeStickyEvent(Class clazz) {
        EventBus.getDefault().removeStickyEvent(clazz);
    }
}
