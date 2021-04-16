package com.hjq.toast.demo;

import android.app.Application;

import com.hjq.toast.ToastUtils;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : ToastUtils 初始化
 */
public final class ToastApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 设置 Toast 拦截器
        ToastUtils.setInterceptor(new ToastInterceptor());
        // 初始化 Toast 框架
        ToastUtils.init(this);
    }
}