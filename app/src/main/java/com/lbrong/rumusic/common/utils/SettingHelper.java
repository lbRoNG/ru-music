package com.lbrong.rumusic.common.utils;

import android.text.TextUtils;

import com.lbrong.rumusic.common.type.PlayMethodEnum;
import com.tencent.mmkv.MMKV;

/**
 * @author lbRoNG
 * @since 2018/10/26
 * 配置帮助类
 */
public final class SettingHelper {
    private static SettingHelper settingHelper;
    private static MMKV kv;

    // app第一次打开
    private final String FIRST_START = "first_start";
    // 自动播放下一首
    private final String MUSIC_AUTO_NEXT = "music_auto_next";
    // 播放方式
    private final String MUSIC_PLAY_METHOD = "music_play_method";

    private SettingHelper() {}

    public static SettingHelper build() {
        if (settingHelper == null) {
            synchronized (SettingHelper.class) {
                if (settingHelper == null) {
                    settingHelper = new SettingHelper();
                    kv = MMKV.defaultMMKV();
                }
            }
        }
        return settingHelper;
    }

    /**
     * 配置是否第一次打开
     */
    public SettingHelper first(){
        kv.encode(FIRST_START,false);
        return settingHelper;
    }

    /**
     * 配置自动播放
     */
    public SettingHelper autoNext(boolean auto){
        kv.encode(MUSIC_AUTO_NEXT,auto);
        return settingHelper;
    }

    /**
     * 配置播放方式
     */
    public SettingHelper playMethod(PlayMethodEnum methodEnum){
        kv.encode(MUSIC_PLAY_METHOD,methodEnum.toString());
        return settingHelper;
    }

    /**
     * 获取是否自动播放
     */
    public boolean isAutoNext(){
        return kv.decodeBool(MUSIC_AUTO_NEXT);
    }

    /**
     * 获取是否第一次打开app
     */
    public boolean isFirst(){
        return !kv.contains(FIRST_START);
    }

    /**
     * 获取播放方式
     */
    public PlayMethodEnum getPlayMethod(){
        String code = kv.decodeString(MUSIC_PLAY_METHOD);
        if(TextUtils.equals(code,PlayMethodEnum.ORDER.toString())){
            return PlayMethodEnum.ORDER;
        } else if(TextUtils.equals(code,PlayMethodEnum.ORDER_LOOP.toString())){
            return PlayMethodEnum.ORDER_LOOP;
        } else if(TextUtils.equals(code,PlayMethodEnum.RANDOM.toString())){
            return PlayMethodEnum.RANDOM;
        } else if(TextUtils.equals(code,PlayMethodEnum.SINGLE.toString())){
            return PlayMethodEnum.SINGLE;
        }
        return PlayMethodEnum.ORDER;
    }
}
