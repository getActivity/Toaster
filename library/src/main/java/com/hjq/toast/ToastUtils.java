package com.hjq.toast;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.toast.style.ToastBlackStyle;
import com.hjq.toast.style.ToastQQStyle;
import com.hjq.toast.style.ToastWhiteStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : Toast 工具类
 */
public final class ToastUtils {

    /** Toast 拦截器 */
    private static IToastInterceptor sInterceptor;

    /** Toast 处理策略 */
    private static IToastStrategy sStrategy;

    /** Toast 单例对象 */
    private static Toast sToast;

    /**
     * 不允许被外部实例化
     */
    private ToastUtils() {}

    /**
     * 初始化 Toast，需要在 Application.create 中初始化
     *
     * @param application       应用的上下文
     */
    public static void init(Application application) {
        init(application, new ToastBlackStyle(application));
    }

    /**
     * 初始化 Toast 及样式
     */
    public static void init(Application application, IToastStyle style) {
        checkNullPointer(application);
        // 初始化 Toast 拦截器
        if (sInterceptor == null) {
            setToastInterceptor(new ToastInterceptor());
        }

        // 初始化 Toast 显示处理器
        if (sStrategy == null) {
            setToastStrategy(new ToastStrategy());
        }

        // 创建 Toast 对象
        setToast(sStrategy.create(application));

        // 设置 Toast 视图
        setView(createTextView(application, style));

        // 设置 Toast 重心
        setGravity(style.getGravity(), style.getXOffset(), style.getYOffset());
    }

    /**
     * 显示一个对象的吐司
     *
     * @param object      对象
     */
    public static void show(Object object) {
        show(object != null ? object.toString() : "null");
    }

    /**
     * 显示一个吐司
     *
     * @param id      如果传入的是正确的 string id 就显示对应字符串
     *                如果不是则显示一个整数的string
     */
    public static void show(int id) {
        checkToastState();

        try {
            // 如果这是一个资源 id
            show(getContext().getResources().getText(id));
        } catch (Resources.NotFoundException ignored) {
            // 如果这是一个 int 整数
            show(String.valueOf(id));
        }
    }

    /**
     * 显示一个吐司
     *
     * @param text      需要显示的文本
     */
    public static synchronized void show(CharSequence text) {
        checkToastState();

        if (sInterceptor.intercept(sToast, text)) {
            return;
        }

        sStrategy.show(text);
    }

    /**
     * 取消吐司的显示
     */
    public static synchronized void cancel() {
        checkToastState();

        sStrategy.cancel();
    }

    /**
     * 设置吐司的位置
     *
     * @param gravity           重心
     * @param xOffset           x轴偏移
     * @param yOffset           y轴偏移
     */
    public static void setGravity(int gravity, int xOffset, int yOffset) {
        checkToastState();

        // 适配 Android 4.2 新特性，布局反方向（开发者选项 - 强制使用从右到左的布局方向）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            gravity = Gravity.getAbsoluteGravity(gravity, getContext().getResources().getConfiguration().getLayoutDirection());
        }

        sToast.setGravity(gravity, xOffset, yOffset);
    }

    /**
     * 给当前Toast设置新的布局，具体实现可看{@link NormalToast#setView(View)}
     */
    public static void setView(int id) {
        checkToastState();

        setView(View.inflate(getContext(), id, null));
    }
    public static void setView(View view) {
        checkToastState();

         // 这个 View 不能为空
        checkNullPointer(view);

        // 当前必须用 Application 的上下文创建的 View，否则可能会导致内存泄露
        Context context = view.getContext();
        if (context instanceof Activity || context instanceof Service) {
            throw new IllegalArgumentException("The view must be initialized using the context of the application");
        }

        // 如果吐司已经创建，就重新初始化吐司
        if (sToast != null) {
            // 取消原有吐司的显示
            sToast.cancel();
            sToast.setView(view);
        }
    }

    /**
     * 获取当前 Toast 的视图
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> V getView() {
        checkToastState();

        return (V) sToast.getView();
    }

    /**
     * 初始化全局的Toast样式
     *
     * @param style         样式实现类，框架已经实现三种不同的样式
     *                      黑色样式：{@link ToastBlackStyle}
     *                      白色样式：{@link ToastWhiteStyle}
     *                      仿QQ样式：{@link ToastQQStyle}
     */
    public static void initStyle(IToastStyle style) {
        checkNullPointer(style);
        // 如果吐司已经创建，就重新初始化吐司
        if (sToast != null) {
            // 取消原有吐司的显示
            sToast.cancel();
            sToast.setView(createTextView(getContext(), style));
            sToast.setGravity(style.getGravity(), style.getXOffset(), style.getYOffset());
        }
    }

    /**
     * 设置当前Toast对象
     */
    public static void setToast(Toast toast) {
        checkNullPointer(toast);
        if (sToast != null && toast.getView() == null) {
            // 移花接木
            toast.setView(sToast.getView());
            toast.setGravity(sToast.getGravity(), sToast.getXOffset(), sToast.getYOffset());
            toast.setMargin(sToast.getHorizontalMargin(), sToast.getVerticalMargin());
        }
        sToast = toast;
        if (sStrategy != null) {
            sStrategy.bind(sToast);
        }
    }

    /**
     * 设置 Toast 显示策略
     */
    public static void setToastStrategy(IToastStrategy strategy) {
        checkNullPointer(strategy);
        sStrategy = strategy;
        if (sToast != null) {
            sStrategy.bind(sToast);
        }
    }

    /**
     * 设置 Toast 拦截器（可以根据显示的内容决定是否拦截这个Toast）
     * 场景：打印 Toast 内容日志、根据 Toast 内容是否包含敏感字来动态切换其他方式显示（这里可以使用我的另外一套框架 XToast）
     */
    public static void setToastInterceptor(IToastInterceptor interceptor) {
        checkNullPointer(interceptor);
        sInterceptor = interceptor;
    }

    /**
     * 获取当前Toast对象
     */
    public static Toast getToast() {
        return sToast;
    }

    /**
     * 检查吐司状态，如果未初始化请先调用{@link ToastUtils#init(Application)}
     */
    private static void checkToastState() {
        // 吐司工具类还没有被初始化，必须要先调用init方法进行初始化
        if (sToast == null) {
            throw new IllegalStateException("ToastUtils has not been initialized");
        }
    }

    /**
     * 检查对象是否为空
     */
    private static void checkNullPointer(Object object) {
        if (object == null) {
            throw new NullPointerException("are you ok?");
        }
    }

    /**
     * 根据样式生成默认的 TextView 对象
     */
    private static TextView createTextView(Context context, IToastStyle style) {
        TextView textView = new TextView(context);
        textView.setId(android.R.id.message);
        textView.setTextColor(style.getTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getTextSize());

        // 适配布局反方向特性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setPaddingRelative(style.getPaddingStart(), style.getPaddingTop(), style.getPaddingEnd(), style.getPaddingBottom());
        } else {
            textView.setPadding(style.getPaddingStart(), style.getPaddingTop(), style.getPaddingEnd(), style.getPaddingBottom());
        }

        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        GradientDrawable drawable = new GradientDrawable();
        // 设置背景色
        drawable.setColor(style.getBackgroundColor());
        // 设置圆角大小
        drawable.setCornerRadius(style.getCornerRadius());

        // setBackground API 版本兼容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(drawable);
        } else {
            textView.setBackgroundDrawable(drawable);
        }

        // 设置 Z 轴阴影
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setZ(style.getZ());
        }

        // 设置最大显示行数
        if (style.getMaxLines() > 0) {
            textView.setMaxLines(style.getMaxLines());
        }

        return textView;
    }

    /**
     * 获取上下文对象
     */
    private static Context getContext() {
        checkToastState();
        return sToast.getView().getContext();
    }
}