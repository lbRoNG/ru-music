package com.lbrong.rumusic.common.net.rx.subscriber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.WindowManager;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lbrong.rumusic.common.utils.ObjectHelper;

import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 网络请求加载框帮助类
 */
@SuppressLint("StaticFieldLeak")
class ProgressDialogHelper {
    private OnProgressCancelListener mListener;
    private String content;
    private long showTime;
    private Disposable disposable;
    private MaterialDialog mDialog;
    private Context context;

    interface OnProgressCancelListener {
        void onCancelProgress();
    }

    ProgressDialogHelper(Context c, String s, OnProgressCancelListener l) {
        super();
        context = c;
        mListener = l;
        content = s;
        initProgressDialog();
    }

    private void initProgressDialog() {
        if (mDialog == null) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

            mDialog = builder
                    .progress(true, 0)
                    .backgroundColor(Color.TRANSPARENT)
                    .canceledOnTouchOutside(false)
                    .build();
        }
    }

    void showDialog() {
        if (mDialog != null && !mDialog.isShowing()) {
            showTime = System.currentTimeMillis();
            mDialog.setContent(content);
            mDialog.show();
        }
    }

    void promptlyDismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @SuppressLint("CheckResult")
    void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            long nowTime = System.currentTimeMillis();
            if (nowTime - showTime > 300) {
                mDialog.dismiss();
            } else {
                // 各种问题，等待好的解决方案
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }

                // 延迟，防止请求过快对话框闪过
                disposable = Observable.timer(300, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) {
                                // 因为是延时任务，防止context已经被销毁
                                if (ObjectHelper.requireNonNull(context)) {
                                    if (context instanceof Activity) {
                                        Activity temp = (Activity) context;
                                        if (!temp.isFinishing()) {
                                            mDialog.dismiss();
                                        } else {
                                            mListener.onCancelProgress();
                                            mDialog = null;
                                        }
                                    }
                                }
                            }
                        });
            }
        }
    }
}
