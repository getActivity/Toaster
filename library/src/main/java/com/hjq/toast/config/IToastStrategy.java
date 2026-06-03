package com.hjq.toast.config;

import androidx.annotation.NonNull;
import com.hjq.toast.ToastParams;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2019/05/19
 *    desc   : Toast 处理策略
 */
public interface IToastStrategy {

    /**
     * 计算 Toast 显示时长
     */
    int computeShowDuration(@NonNull CharSequence text);

    /**
     * 创建 Toast
     */
    IToast createToast(@NonNull ToastParams params);

    /**
     * 显示 Toast
     */
    void showToast(@NonNull ToastParams params);

    /**
     * 取消 Toast
     */
    void cancelToast();
}