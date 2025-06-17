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

    /** 优先级类型：框架默认选择最优方案来显示，具体实现可以看一下 {@link ToastStrategy#createToast(ToastParams)} */
    public static final int PRIORITY_TYPE_DEFAULT = 0;
    /** 优先级类型：优先使用全局级 Toast 来显示（显示在所有应用上面，可能需要通知栏权限或者悬浮窗权限） */
    public static final int PRIORITY_TYPE_GLOBAL = 1;
    /** 优先级类型：优先使用局部的 Toast 来显示（显示在当前 Activity） */
    public static final int PRIORITY_TYPE_LOCAL = 2;

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

    /** 优先级类型 */
    public int priorityType = PRIORITY_TYPE_DEFAULT;

    /** Toast 样式 */
    public IToastStyle<?> style;

    /** Toast 处理策略 */
    public IToastStrategy strategy;

    /** Toast 拦截器 */
    public IToastInterceptor interceptor;
}