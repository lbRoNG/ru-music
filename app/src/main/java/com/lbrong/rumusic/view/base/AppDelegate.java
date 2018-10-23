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
package com.lbrong.rumusic.view.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lbrong.rumusic.iface.view.IErrorView;

/**
 * View delegate base class
 * 视图层代理的基类
 */
public abstract class AppDelegate implements IDelegate {

    protected final SparseArray<View> mViews = new SparseArray<View>();
    protected View rootView;

    public abstract int getRootLayoutId();

    @Override
    public void create(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int rootLayoutId = getRootLayoutId();
        rootView = inflater.inflate(rootLayoutId, container, false);
    }

    @Override
    public int getOptionsMenuId() {
        return 0;
    }

    @Override
    public Toolbar getToolbar() {
        return null;
    }

    @Override
    public View getRootView() {
        return rootView;
    }

    @Override
    public void initWidget() { }

    @Override
    public IErrorView getErrorView() {
        return null;
    }

    @Override
    public TextView getTitleView() {
        return null;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public <T extends View> T bindView(int id) {
        T view = (T) mViews.get(id);
        if (view == null) {
            view = (T) rootView.findViewById(id);
            mViews.put(id, view);
        }
        return view;
    }

    public <T extends View> T get(int id) {
        return (T) bindView(id);
    }

    public void setOnClickListener(View.OnClickListener listener, int... ids) {
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            get(id).setOnClickListener(listener);
        }
    }

    public <T extends EditText> void setOnTextChangeListener(TextWatcher watcher, int... ids) {
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            T view = get(id);
            view.addTextChangedListener(watcher);
        }
    }

    public void toast(CharSequence msg) {
        Toast.makeText(rootView.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void toast(@StringRes int strId) {
        Toast.makeText(rootView.getContext(), strId, Toast.LENGTH_SHORT).show();
    }

    public void snack(View root,CharSequence msg) {
        Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void snack(View root,@StringRes int strId) {
        Snackbar.make(root, strId, Snackbar.LENGTH_SHORT).show();
    }

    public <T extends Activity> T getActivity() {
        return (T) rootView.getContext();
    }
}
