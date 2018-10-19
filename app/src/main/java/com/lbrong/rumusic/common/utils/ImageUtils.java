package com.lbrong.rumusic.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * @author lbRoNG
 * @since 2018/10/19
 */
public final class ImageUtils {
    private ImageUtils() { }

    public static Bitmap compressBitmap(Bitmap bmp,int ratio) {
        // 创建个空的bitmap
        Bitmap result = Bitmap.createBitmap(bmp.getWidth() / ratio, bmp.getHeight() / ratio, Bitmap.Config.ARGB_8888);
        // 把空的bitmap给画板
        Canvas canvas = new Canvas(result);
        // 要画多大
        RectF rect = new RectF(0, 0, bmp.getWidth() / ratio, bmp.getHeight() / ratio);
        canvas.drawBitmap(bmp, null, rect, null);
        bmp.recycle();
        return result;
    }
}
