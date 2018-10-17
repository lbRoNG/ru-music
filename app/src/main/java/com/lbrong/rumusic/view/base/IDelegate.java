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

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lbrong.rumusic.iface.view.IErrorView;

/**
 * View delegate base class
 * 视图层代理的接口协议
 */
public interface IDelegate {
    /**
     * 创建布局
     */
    void create(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 菜单
     */
    int getOptionsMenuId();

    /**
     * 标题栏
     */
    Toolbar getToolbar();

    /**
     * 根布局
     */
    View getRootView();

    /**
     * 初始化
     */
    void initWidget();

    /**
     * 获取错误布局
     */
    IErrorView getErrorView();

    /**
     * 自定义标题栏TextView
     */
    TextView getTitleView();
}
