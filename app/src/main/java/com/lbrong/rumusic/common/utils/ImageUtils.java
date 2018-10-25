package com.lbrong.rumusic.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

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

    /**
     * Drawable转换为Bitmap
     * */
    public static Bitmap drawableToBitmap(Drawable drawable){
        if(drawable==null)
            return null;
        if(drawable instanceof BitmapDrawable)
            return ((BitmapDrawable)drawable).getBitmap();

        Bitmap bitmap;
        //如果是ColorDrawable随便给一个宽高
        if(drawable instanceof ColorDrawable)
            bitmap=Bitmap.createBitmap(2,2,Bitmap.Config.ARGB_8888);
        else
            bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);

        Canvas canvas=new Canvas(bitmap);
        //设置绘制的矩形区域
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
