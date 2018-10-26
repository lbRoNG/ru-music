/*
 * Copyright (c) 2015, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lbrong.rumusic.presenter.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lbrong.rumusic.common.net.RequestHelper;
import com.lbrong.rumusic.common.net.api.ApiService;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.view.base.IDelegate;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Presenter base class for Fragment
 * Presenter层的实现基类
 * @param <T> View delegate class type
 */
public abstract class FragmentPresenter<T extends IDelegate> extends RxFragment {
    public T viewDelegate;
    protected ApiService apiService;
    private CompositeDisposable disposableContainer;
    private AtomicInteger waitTaskCount;
    private Disposable waitDisposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            apiService = RequestHelper.getInstance().getApiService();
            viewDelegate = getDelegateClass().newInstance();
            disposableContainer = new CompositeDisposable();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewDelegate.create(inflater, container, savedInstanceState);
        return viewDelegate.getRootView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewDelegate.initWidget();
        bindEvenListener();
        initLiveDataObserver();
        init();
    }

    protected void init(){ }

    protected void bindEvenListener() { }

    protected void initLiveDataObserver() { }

    public <T> LifecycleTransformer<T> bindToLife(FragmentEvent event) {
        return bindUntilEvent(event);
    }

    /**
     * 等待所有任务完成
     * @param taskCount 等待任务数量
     */
    protected void startWaitAllTask(int taskCount){
        cancelWaitTask();

        waitTaskCount = new AtomicInteger(taskCount);

        waitDisposable = Observable.interval(0,1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong){
                        if(waitTaskCount.get() == 0){
                            onTaskAllComplete();
                        }
                    }
                });
    }

    /**
     * 等待任务完成一个
     */
    protected void completeOne(){
        if(ObjectHelper.requireNonNull(waitTaskCount)
                && waitTaskCount.get() >= 1){
            waitTaskCount.set(waitTaskCount.get() - 1);
        }
    }

    /**
     * 等待任务完成全部的回调
     */
    protected void onTaskAllComplete(){
        cancelWaitTask();
    }

    /**
     * 取消等待任务
     */
    protected void cancelWaitTask(){
        if(ObjectHelper.requireNonNull(waitDisposable) && !waitDisposable.isDisposed()){
            waitDisposable.dispose();
            waitDisposable = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (viewDelegate.getOptionsMenuId() != 0) {
            inflater.inflate(viewDelegate.getOptionsMenuId(), menu);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (viewDelegate == null) {
            try {
                viewDelegate = getDelegateClass().newInstance();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异步任务保存
     */
    protected void addDisposable(Disposable disposable){
        if(disposableContainer != null && disposable != null){
            disposableContainer.add(disposable);
        }
    }

    /**
     * 取消异步任务
     */
    protected void clearDisposable(){
        if(disposableContainer != null){
            disposableContainer.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewDelegate = null;
        apiService = null;

        if(disposableContainer != null){
            disposableContainer.clear();
            disposableContainer = null;
        }

        cancelWaitTask();
    }

    protected abstract Class<T> getDelegateClass();
}
