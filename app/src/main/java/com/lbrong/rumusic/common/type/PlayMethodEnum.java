package com.lbrong.rumusic.common.type;

/**
 * @author lbRoNG
 * @since 2018/8/9
 * 播放方式
 */
public enum PlayMethodEnum {
    // 顺序
    ORDER(1),
    // 顺序循环
    ORDER_LOOP(2),
    // 随机
    RANDOM(3),
    // 单曲循环
    SINGLE(4);

    private int code;

    PlayMethodEnum(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return String.valueOf(code);
    }
}
