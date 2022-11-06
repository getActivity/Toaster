package com.hjq.toast;

import com.hjq.toast.config.IToastInterceptor;
import com.hjq.toast.config.IToastStrategy;
import com.hjq.toast.config.IToastStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2022/10/31
 *    desc   : Toast 参数类
 */
public final class ToastParams {

    /** 显示的文本 */
    public CharSequence text;

    /** 延迟时间 */
    public long delayMillis = 0;

    /** 显示时长 */
    public int toastDuration = -1;

    /** Toast 样式 */
    public IToastStyle<?> style;

    /** Toast 拦截器 */
    public IToastInterceptor interceptor;

    /** Toast 策略 */
    public IToastStrategy strategy;
}