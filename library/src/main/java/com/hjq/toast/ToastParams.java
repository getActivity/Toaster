package com.hjq.toast;

import com.hjq.toast.config.IToastInterceptor;
import com.hjq.toast.config.IToastStrategy;
import com.hjq.toast.config.IToastStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2022/10/31
 *    desc   : Toast 参数类
 */
public class ToastParams {

    /** 显示的文本 */
    public CharSequence text;

    /**
     * Toast 显示时长，有两种值可选
     *
     * 短吐司：{@link android.widget.Toast#LENGTH_SHORT}
     * 长吐司：{@link android.widget.Toast#LENGTH_LONG}
     */
    public int duration = -1;

    /** 延迟显示时间 */
    public long delayMillis = 0;

    /** 是否跨页面展示（如果为 true 则优先用系统 Toast 实现） */
    public boolean crossPageShow;

    /** Toast 样式 */
    public IToastStyle<?> style;

    /** Toast 处理策略 */
    public IToastStrategy strategy;

    /** Toast 拦截器 */
    public IToastInterceptor interceptor;
}