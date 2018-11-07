package com.hjq.toast.demo;

import android.app.Application;

import com.hjq.toast.ToastUtils;

public class ToastApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化吐司
        ToastUtils.init(this);
    }
}