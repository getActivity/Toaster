package com.hjq.toast;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.widget.Toast;

import com.hjq.toast.config.IToastInterceptor;
import com.hjq.toast.config.IToastStrategy;
import com.hjq.toast.config.IToastStyle;
import com.hjq.toast.style.BlackToastStyle;
import com.hjq.toast.style.CustomToastStyle;
import com.hjq.toast.style.LocationToastStyle;
import com.hjq.toast.style.WhiteToastStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2018/09/01
 *    desc   : Toast 框架（专治 Toast 疑难杂症）
 */
@SuppressWarnings("unused")
public final class Toaster {

    /** Application 对象 */
    private static Application sApplication;

    /** Toast 处理策略 */
    private static IToastStrategy sToastStrategy;

    /** Toast 样式 */
    private static IToastStyle<?> sToastStyle;

    /** Toast 拦截器（可空） */
    private static IToastInterceptor sToastInterceptor;

    /** 调试模式 */
    private static Boolean sDebugMode;

    /**
     * 不允许被外部实例化
     */
    private Toaster() {}

    /**
     * 初始化 Toast，需要在 Application.create 中初始化
     *
     * @param application       应用的上下文
     */
    public static void init(Application application) {
        init(application, sToastStyle);
    }

    public static void init(Application application, IToastStrategy strategy) {
        init(application, strategy, null);
    }

    public static void init(Application application, IToastStyle<?> style) {
        init(application, null, style);
    }

    /**
     * 初始化 Toast
     *
     * @param application       应用的上下文
     * @param strategy          Toast 策略
     * @param style             Toast 样式
     */
    public static void init(Application application, IToastStrategy strategy, IToastStyle<?> style) {
        sApplication = application;

        // 初始化 Toast 策略
        if (strategy == null) {
            strategy = new ToastStrategy();
        }
        setStrategy(strategy);

        // 设置 Toast 样式
        if (style == null) {
            style = new BlackToastStyle();
        }
        setStyle(style);
    }

    /**
     * 判断当前框架是否已经初始化
     */
    public static boolean isInit() {
        return sApplication != null && sToastStrategy != null && sToastStyle != null;
    }

    /**
     * 延迟显示 Toast
     */

    public static void delayedShow(int id, long delayMillis) {
        delayedShow(stringIdToCharSequence(id), delayMillis);
    }

    public static void delayedShow(Object object, long delayMillis) {
        delayedShow(objectToCharSequence(object), delayMillis);
    }

    public static void delayedShow(CharSequence text, long delayMillis) {
        ToastParams params = new ToastParams();
        params.text = text;
        params.delayMillis = delayMillis;
        show(params);
    }

    /**
     * debug 模式下显示 Toast
     */

    public static void debugShow(int id) {
        debugShow(stringIdToCharSequence(id));
    }

    public static void debugShow(Object object) {
        debugShow(objectToCharSequence(object));
    }

    public static void debugShow(CharSequence text) {
        if (!isDebugMode()) {
            return;
        }
        ToastParams params = new ToastParams();
        params.text = text;
        show(params);
    }

    /**
     * 显示一个短 Toast
     */

    public static void showShort(int id) {
        showShort(stringIdToCharSequence(id));
    }

    public static void showShort(Object object) {
        showShort(objectToCharSequence(object));
    }

    public static void showShort(CharSequence text) {
        ToastParams params = new ToastParams();
        params.text = text;
        params.duration = Toast.LENGTH_SHORT;
        show(params);
    }

    /**
     * 显示一个长 Toast
     */

    public static void showLong(int id) {
        showLong(stringIdToCharSequence(id));
    }

    public static void showLong(Object object) {
        showLong(objectToCharSequence(object));
    }

    public static void showLong(CharSequence text) {
        ToastParams params = new ToastParams();
        params.text = text;
        params.duration = Toast.LENGTH_LONG;
        show(params);
    }

    /**
     * 显示 Toast
     */

    public static void show(int id) {
        show(stringIdToCharSequence(id));
    }

    public static void show(Object object) {
        show(objectToCharSequence(object));
    }

    public static void show(CharSequence text) {
        ToastParams params = new ToastParams();
        params.text = text;
        show(params);
    }

    public static void show(ToastParams params) {
        checkInitStatus();

        // 如果是空对象或者空文本就不显示
        if (params.text == null || params.text.length() == 0) {
            return;
        }

        if (params.strategy == null) {
            params.strategy = sToastStrategy;
        }

        if (params.interceptor == null) {
            if (sToastInterceptor == null) {
                sToastInterceptor = new ToastLogInterceptor();
            }
            params.interceptor = sToastInterceptor;
        }

        if (params.style == null) {
            params.style = sToastStyle;
        }

        if (params.interceptor.intercept(params)) {
            return;
        }

        if (params.duration == -1) {
            params.duration = params.text.length() > 20 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        }

        params.strategy.showToast(params);
    }

    /**
     * 取消吐司的显示
     */
    public static void cancel() {
        sToastStrategy.cancelToast();
    }

    /**
     * 设置吐司的位置
     *
     * @param gravity           重心
     */
    public static void setGravity(int gravity) {
        setGravity(gravity, 0, 0);
    }

    public static void setGravity(int gravity, int xOffset, int yOffset) {
        setGravity(gravity, xOffset, yOffset, 0, 0);
    }

    public static void setGravity(int gravity, int xOffset, int yOffset, float horizontalMargin, float verticalMargin) {
        sToastStyle = new LocationToastStyle(sToastStyle, gravity, xOffset, yOffset, horizontalMargin, verticalMargin);
    }

    /**
     * 给当前 Toast 设置新的布局
     */
    public static void setView(int id) {
        if (id <= 0) {
            return;
        }
        setStyle(new CustomToastStyle(id, sToastStyle.getGravity(),
                sToastStyle.getXOffset(), sToastStyle.getYOffset(),
                sToastStyle.getHorizontalMargin(), sToastStyle.getVerticalMargin()));
    }

    /**
     * 初始化全局的 Toast 样式
     *
     * @param style         样式实现类，框架已经实现两种不同的样式
     *                      黑色样式：{@link BlackToastStyle}
     *                      白色样式：{@link WhiteToastStyle}
     */
    public static void setStyle(IToastStyle<?> style) {
        sToastStyle = style;
    }

    public static IToastStyle<?> getStyle() {
        return sToastStyle;
    }

    /**
     * 设置 Toast 显示策略
     */
    public static void setStrategy(IToastStrategy strategy) {
        sToastStrategy = strategy;
        sToastStrategy.registerStrategy(sApplication);
    }

    public static IToastStrategy getStrategy() {
        return sToastStrategy;
    }

    /**
     * 设置 Toast 拦截器（可以根据显示的内容决定是否拦截这个Toast）
     * 场景：打印 Toast 内容日志、根据 Toast 内容是否包含敏感字来动态切换其他方式显示（这里可以使用我的另外一套框架 EasyWindow）
     */
    public static void setInterceptor(IToastInterceptor interceptor) {
        sToastInterceptor = interceptor;
    }

    public static IToastInterceptor getInterceptor() {
        return sToastInterceptor;
    }

    /**
     * 是否为调试模式
     */
    public static void setDebugMode(boolean debug) {
        sDebugMode = debug;
    }

    /**
     * 检查框架初始化状态，如果未初始化请先调用{@link Toaster#init(Application)}
     */
    private static void checkInitStatus() {
        // 框架当前还没有被初始化，必须要先调用 init 方法进行初始化
        if (sApplication == null) {
            throw new IllegalStateException("Toaster has not been initialized");
        }
    }

    static boolean isDebugMode() {
        if (sDebugMode == null) {
            checkInitStatus();
            sDebugMode = (sApplication.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return sDebugMode;
    }

    private static CharSequence stringIdToCharSequence(int id) {
        checkInitStatus();
        try {
            // 如果这是一个资源 id
            return sApplication.getResources().getText(id);
        } catch (Resources.NotFoundException ignored) {
            // 如果这是一个 int 整数
            return String.valueOf(id);
        }
    }

    private static CharSequence objectToCharSequence(Object object) {
        return object != null ? object.toString() : "null";
    }
}