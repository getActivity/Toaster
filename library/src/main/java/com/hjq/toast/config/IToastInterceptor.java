package com.hjq.toast.config;

import com.hjq.toast.ToastParams;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2019/05/19
 *    desc   : Toast 拦截器接口
 */
public interface IToastInterceptor {

    /**
     * 根据显示的文本决定是否拦截该 Toast
     */
    boolean intercept(ToastParams params);
}