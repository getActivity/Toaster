package com.hjq.toast.demo;

import android.app.Application;

import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastBlackStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : ToastUtils 初始化
 */
public class ToastApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化吐司工具类
        ToastUtils.init(this, new ToastBlackStyle());
    }
}