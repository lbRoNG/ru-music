package com.lbrong.rumusic.common.net.rx.subscriber;

import android.content.Context;

import com.lbrong.rumusic.R;

public abstract class ProgressDialogSubscriber<T>
        extends DefaultSubscriber<T>
        implements ProgressDialogHelper.OnProgressCancelListener {

    private ProgressDialogHelper mHelper;

    public ProgressDialogSubscriber(Context context) {
        mHelper = new ProgressDialogHelper(context, context.getResources().getString(R.string.load), this);
    }

    public ProgressDialogSubscriber(Context context, int strId) {
        mHelper = new ProgressDialogHelper(context, context.getResources().getString(strId), this);
    }

    public ProgressDialogSubscriber(Context context, String content) {
        mHelper = new ProgressDialogHelper(context, content, this);
    }

    /**
     * 是否显示对话框
     */
    protected boolean isShowDialog() {
        return true;
    }

    /**
     * 是否启用延时对话框
     */
    protected boolean isPromptlyDismiss() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isShowDialog()) {
            mHelper.showDialog();
        }
    }

    @Override
    public void onFinish() {
        super.onFinish();
        dismiss();
    }

    @Override
    public void onCancelProgress() {
        dispose();
    }

    private void dismiss() {
        if (isShowDialog()) {
            if (isPromptlyDismiss()) {
                mHelper.promptlyDismissDialog();
            } else {
                mHelper.dismissDialog();
            }
        }
    }

}
