package com.lbrong.rumusic.common.net.rx;

import com.trello.rxlifecycle2.LifecycleProvider;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lbRoNG
 * @since 2018/7/16
 */
@SuppressWarnings("unchecked")
public class RxHelper {

    public static <T> Flowable<T> createData(final T t) {
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<T> emitter){
                emitter.onNext(t);
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static <T> FlowableTransformer<T, T> scheduler() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T,M extends LifecycleProvider> FlowableTransformer<T, T> schedulerOnLifecycle(
            @NonNull final M provider) {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<T> upstream) {
                return upstream
                        .compose(RxHelper.<T>scheduler())
                        .compose(provider.<T>bindToLifecycle());
            }
        };
    }
}
