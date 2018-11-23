package com.lbrong.rumusic.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.lbrong.rumusic.R;

/**
 * @author lbRoNG
 * @since 2018/8/15
 */
public final class PopWindowController {
    private Context context;
    private int width;
    private int height;
    private int offsetX;
    private int offsetY;
    private View content;
    private View parent;
    private int location;
    private boolean changeBgAlpha;
    private boolean outsideTouchable;
    private OnDismissListener onDismissListener;

    public interface OnDismissListener{
        void onDismiss();
    }

    private PopWindowController(@NonNull Builder builder){
        context = builder.context;
        offsetX = builder.offsetX;
        offsetY = builder.offsetY;
        width = builder.width;
        height = builder.height;
        content = builder.content;
        parent = builder.parent;
        location = builder.location;
        changeBgAlpha = builder.changeBgAlpha;
        outsideTouchable = builder.outsideTouchable;
        onDismissListener = builder.onDismissListener;
    }

    /**
     * 底部弹出的window
     */
    public PopupWindow showPopWindowAtLocation() {
        PopupWindow popupWindow = new PopupWindow(content, width, height);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(outsideTouchable);
        popupWindow.setFocusable(outsideTouchable);
        popupWindow.setAnimationStyle(R.style.pop_bottom_anim);
        popupWindow.showAtLocation(parent, location, offsetX, offsetY);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(changeBgAlpha){
                    if (context instanceof Activity) {
                        backgroundAlpha((Activity) context, 1f);
                    }
                }
                if(ObjectHelper.requireNonNull(onDismissListener)){
                    onDismissListener.onDismiss();
                }
            }
        });
        if(changeBgAlpha){
            if (context instanceof Activity) {
                backgroundAlpha((Activity) context, 0.4f);
            }
        }
        return popupWindow;
    }

    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().setAttributes(lp);
    }

    public static class Builder {
        private Context context;
        private int width;
        private int height;
        private int offsetX;
        private int offsetY;
        private View content;
        private View parent;
        private boolean changeBgAlpha = true;
        private int location = Gravity.CENTER;
        private boolean outsideTouchable = true;
        private OnDismissListener onDismissListener;

        public PopWindowController build(){
            return new PopWindowController(this);
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        public Builder setContext(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public Builder setOffsetX(int offsetX) {
            this.offsetX = offsetX;
            return this;
        }

        public Builder setOffsetY(int offsetY) {
            this.offsetY = offsetY;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setContent(@NonNull View content) {
            this.content = content;
            return this;
        }

        public Builder setParent(@NonNull View parent) {
            this.parent = parent;
            return this;
        }

        public Builder setLocation(int location) {
            this.location = location;
            return this;
        }

        public Builder setOutsideTouchable(boolean outsideTouchable) {
            this.outsideTouchable = outsideTouchable;
            return this;
        }

        public Builder setChangeBgAlpha(boolean changeBgAlpha) {
            this.changeBgAlpha = changeBgAlpha;
            return this;
        }
    }

}
