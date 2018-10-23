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
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.lbrong.rumusic.common.net.RequestHelper;
import com.lbrong.rumusic.common.net.api.ApiService;
import com.lbrong.rumusic.common.utils.DensityUtils;
import com.lbrong.rumusic.common.utils.ObjectHelper;
import com.lbrong.rumusic.common.utils.SystemUtils;
import com.lbrong.rumusic.view.base.IDelegate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Presenter base class for Activity
 * Presenter层的实现基类
 * @param <T> View delegate class type
 */
public abstract class ActivityPresenter<T extends IDelegate> extends AppCompatActivity {
    protected T viewDelegate;
    protected ApiService apiService;
    private CompositeDisposable disposableContainer;
    private AtomicInteger waitTaskCount;
    private Disposable waitDisposable;

    public ActivityPresenter() {
        try {
            apiService = RequestHelper.getInstance().getApiService();
            viewDelegate = getDelegateClass().newInstance();
            disposableContainer = new CompositeDisposable();
        } catch (InstantiationException e) {
            throw new RuntimeException("create IDelegate error");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("create IDelegate error");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setting();
        viewDelegate.create(getLayoutInflater(), null, savedInstanceState);
        setContentView(viewDelegate.getRootView());
        if(isFullScreen()){
            SystemUtils.setFullScreen(this);
        }
        setDensityCompat();
        initToolbar();
        viewDelegate.initWidget();
        bindEvenListener();
        init();
        initLiveDataObserver();
    }

    protected void bindEvenListener() { }

    protected void initLiveDataObserver(){}

    protected void setDensityCompat(){
        DensityUtils.setDefault(this);
    }

    protected boolean isFullScreen(){
        return false;
    }

    protected void init(){}

    protected void setting(){}

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

    protected void completeOne(){
        if(ObjectHelper.requireNonNull(waitTaskCount)
                && waitTaskCount.get() >= 1){
            waitTaskCount.set(waitTaskCount.get() - 1);
        }
    }

    protected void onTaskAllComplete(){
        cancelWaitTask();
    }

    protected void cancelWaitTask(){
        if(ObjectHelper.requireNonNull(waitDisposable) && !waitDisposable.isDisposed()){
            waitDisposable.dispose();
            waitDisposable = null;
        }
    }

    protected void initToolbar() {
        Toolbar toolbar = viewDelegate.getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null && displayHomeAsUp()) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        // 设置标题
        // 项目需求原因，标题不使用自带样式，都是自定义居中TextView，所以只提供一种设置方式
        String text = getTitleText();
        int textId = getTitleTextId();
        TextView titleView = viewDelegate.getTitleView();
        if(!TextUtils.isEmpty(text)){
            if(ObjectHelper.requireNonNull(titleView)){
                viewDelegate.getTitleView().setText(text);
            }
        } else if(textId != 0){
            if(ObjectHelper.requireNonNull(titleView)){
                viewDelegate.getTitleView().setText(textId);
            }
        }
    }

    protected void setToolbarTitle(String title){
        if(ObjectHelper.requireNonNull(viewDelegate.getTitleView())){
            viewDelegate.getTitleView().setText(title);
        }
    }

    protected void setToolbarTitle(@StringRes int resId){
        if(ObjectHelper.requireNonNull(viewDelegate.getTitleView())){
            viewDelegate.getTitleView().setText(resId);
        }
    }

    protected String getTitleText(){
        return "";
    }

    protected @StringRes
    int getTitleTextId(){
        return 0;
    }

    protected boolean displayHomeAsUp(){
        return false;
    }

    protected void addDisposable(Disposable disposable){
        if(disposableContainer != null && disposable != null){
            disposableContainer.add(disposable);
        }
    }

    protected void clearDisposable(){
        if(disposableContainer != null){
            disposableContainer.clear();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (viewDelegate == null) {
            try {
                viewDelegate = getDelegateClass().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("create IDelegate error");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("create IDelegate error");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (viewDelegate.getOptionsMenuId() != 0) {
            getMenuInflater().inflate(viewDelegate.getOptionsMenuId(), menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewDelegate = null;
        apiService = null;
        if((ObjectHelper.requireNonNull(disposableContainer))){
            disposableContainer.clear();
            disposableContainer = null;
        }
        cancelWaitTask();
    }

    protected abstract Class<T> getDelegateClass();
}
