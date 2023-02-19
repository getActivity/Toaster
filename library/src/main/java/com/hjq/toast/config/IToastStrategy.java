package com.hjq.toast.config;

import android.app.Application;

import com.hjq.toast.ToastParams;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2019/05/19
 *    desc   : Toast 处理策略
 */
public interface IToastStrategy {

    /**
     * 注册策略
     */
    void registerStrategy(Application application);

    /**
     * 创建 Toast
     */
    IToast createToast(IToastStyle<?> style);

    /**
     * 显示 Toast
     */
    void showToast(ToastParams params);

    /**
     * 取消 Toast
     */
    void cancelToast();
}