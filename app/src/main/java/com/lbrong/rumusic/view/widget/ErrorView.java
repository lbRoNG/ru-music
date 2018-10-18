package com.lbrong.rumusic.view.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lbrong.rumusic.R;
import com.lbrong.rumusic.common.net.exception.ExceptionConstant;
import com.lbrong.rumusic.common.type.ErrorViewStateEnum;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.iface.listener.OnErrorClickReloadListener;
import com.lbrong.rumusic.iface.view.IErrorView;

/**
 * @author lbRoNG
 * @since 2018/8/9
 * 默认错误填充界面
 */
public class ErrorView extends FrameLayout implements IErrorView {

    private TextView tvText;
    private ImageView ivImg;
    private ErrorViewStateEnum state = ErrorViewStateEnum.NONE_DATA;
    private OnErrorClickReloadListener clickReloadListener;

    public ErrorView(@NonNull Context context) {
        this(context,null);
    }

    public ErrorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        initView();
        initListener();
        // 设置默认显示
        hide();
    }

    protected void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.widget_error_view,this,true);
        tvText = getChildAt(0).findViewById(R.id.tv_text);
        ivImg = getChildAt(0).findViewById(R.id.iv_img);
    }

    protected void initListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ObjectHelper.requireNonNull(clickReloadListener)){
                    if(getChildCount() > 0 && getChildAt(0).getVisibility() == VISIBLE){
                        clickReloadListener.onReload();
                    }
                }
            }
        });

    }

    @Override
    public IErrorView setClickReloadListener(@Nullable OnErrorClickReloadListener listener) {
        this.clickReloadListener = listener;
        return this;
    }

    @Override
    public void show(){
        setContentOfState();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(i == 0 ? VISIBLE : INVISIBLE);
        }
    }

    public void show(@NonNull ErrorViewStateEnum state){
        this.state = state;
        show();
    }

    public void show(int exceptionCode){
        setState(exceptionCode);
        show();
    }

    @Override
    public void hide(){
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setVisibility(i == 0 ? GONE : VISIBLE);
        }
    }

    public ErrorView setState(@NonNull ErrorViewStateEnum state){
        this.state = state;
        return this;
    }

    public ErrorView setState(int exceptionCode){
        switch (exceptionCode){
            case ExceptionConstant.ERROR_NOT_NETWORK:
            case ExceptionConstant.ERROR_SOCKET_TIMEOUT:
                this.state = ErrorViewStateEnum.NONE_NETWORK;
                break;
            case ExceptionConstant.ERROR_HTTP_500:
                this.state = ErrorViewStateEnum.SERVER_ERROR;
                break;
            default:
                this.state = ErrorViewStateEnum.NONE_DATA;
                break;
        }
        return this;
    }

    public ErrorView setBackground(@ColorInt int color){
        getChildAt(0).setBackgroundColor(color);
        return this;
    }

    public ErrorView setText(@Nullable String text){
        state = ErrorViewStateEnum.CUSTOM;
        tvText.setText(text);
        return this;
    }

    public ErrorView setText(@StringRes int textId){
        state = ErrorViewStateEnum.CUSTOM;
        tvText.setText(textId);
        return this;
    }

    public ErrorView setImg(@DrawableRes int resId){
        ivImg.setImageResource(resId);
        return this;
    }

    protected void setContentOfState(){
        switch (state){
            case NONE_DATA:
                setText(R.string.none_data);
                break;
            case SERVER_ERROR:
                setText(R.string.server_error);
                break;
            case NONE_NETWORK:
                setText(R.string.none_network);
                break;
        }
    }
}
