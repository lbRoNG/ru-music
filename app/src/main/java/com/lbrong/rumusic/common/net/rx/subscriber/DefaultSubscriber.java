package com.lbrong.rumusic.common.net.rx.subscriber;

import io.reactivex.subscribers.ResourceSubscriber;

public class DefaultSubscriber<T> extends ResourceSubscriber<T> {

    @Override
    public void onNext(T t) {}

    @Override
    public void onError(Throwable t) {
        onFinish();
    }

    @Override
    public void onComplete() {
        onFinish();
    }

    /**
     * 不管成功还是失败，请求结束调用
     */
    public void onFinish(){}

}
