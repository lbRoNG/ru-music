package com.lbrong.rumusic.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lbRoNG
 * @since 2018/7/17
 */
public final class DateUtils {
    private DateUtils(){}

    /**
     * 获取当前日期的字符串
     * @param format 格式
     */
    public static String getTodayDateString(String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    /**
     * 获取传入日期的字符串
     * @param time 时间
     * @param format 格式
     */
    public static String getDateString(long time, String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(time));
    }

    /**
     * 秒数转换完整时分秒
     */
    public static String secondForFullTime(int second) {
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }

        String hour = h < 10 ? "0" + h : h + "";
        String minute = d < 10 ? "0" + d : d + "";
        String seconds = s < 10 ? "0" +  s :  s + "";

        return hour + ":" + minute + ":" + seconds;
    }
}
