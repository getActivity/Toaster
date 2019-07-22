package com.hjq.toast;

import android.app.AppOpsManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : Toast 工具类
 */
public final class ToastUtils {

    private static IToastInterceptor sInterceptor;

    private static IToastStrategy sStrategy;

    private static IToastStyle sStyle;

    private static Toast sToast;

    /**
     * 不允许外部实例化
     */
    private ToastUtils() {}

    /**
     * 初始化 ToastUtils 及样式
     */
    public static void init(Application application, IToastStyle style) {
        initStyle(style);
        init(application);
    }

    /**
     * 初始化 ToastUtils，在Application中初始化
     *
     * @param application       应用的上下文
     */
    public static void init(Application application) {
        checkNullPointer(application);
        // 初始化 Toast 拦截器
        if (sInterceptor == null) {
            setToastInterceptor(new ToastInterceptor());
        }

        // 初始化 Toast 显示处理器
        if (sStrategy == null) {
            setToastHandler(new ToastStrategy());
        }

        // 初始化 Toast 样式
        if (sStyle == null) {
             initStyle(new ToastBlackStyle(application));
        }

        // 初始化吐司
        if (isNotificationEnabled(application)) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                // 解决 Android 7.1 上主线程被阻塞后吐司会报错的问题
                setToast(new SafeToast(application));
            } else {
                setToast(new BaseToast(application));
            }
        } else {
            // 解决关闭通知栏权限后 Toast 不显示的问题
            setToast(new SupportToast(application));
        }

        // 初始化布局
        setView(createTextView(application.getApplicationContext()));

        // 初始化位置
        setGravity(sStyle.getGravity(), sStyle.getXOffset(), sStyle.getYOffset());
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
     * @param id      如果传入的是正确的string id就显示对应字符串
     *                如果不是则显示一个整数的string
     */
    public static void show(int id) {
        checkToastState();

        try {
            // 如果这是一个资源 id
            show(sToast.getView().getContext().getResources().getText(id));
        } catch (Resources.NotFoundException ignored) {
            // 如果这是一个 int 数据
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
            gravity = Gravity.getAbsoluteGravity(gravity, sToast.getView().getResources().getConfiguration().getLayoutDirection());
        }

        sToast.setGravity(gravity, xOffset, yOffset);
    }

    /**
     * 给当前Toast设置新的布局，具体实现可看{@link BaseToast#setView(View)}
     */
    public static void setView(int layoutId) {
        checkToastState();

        setView(View.inflate(sToast.getView().getContext().getApplicationContext(), layoutId, null));
    }
    public static void setView(View view) {
        checkToastState();

         // 这个 View 不能为空
        checkNullPointer(view);

        // 当前必须用 Application 的上下文创建的 View，否则可能会导致内存泄露
        Context context = view.getContext();
        if (!(context instanceof Application)) {
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
        ToastUtils.sStyle = style;
        // 如果吐司已经创建，就重新初始化吐司
        if (sToast != null) {
            // 取消原有吐司的显示
            sToast.cancel();
            sToast.setView(createTextView(sToast.getView().getContext().getApplicationContext()));
            sToast.setGravity(sStyle.getGravity(), sStyle.getXOffset(), sStyle.getYOffset());
        }
    }

    /**
     * 设置当前Toast对象
     */
    public static void setToast(Toast toast) {
        checkNullPointer(toast);
        sToast = toast;
        if (sStrategy != null) {
            sStrategy.bind(sToast);
        }
    }

    /**
     * 设置 Toast 显示规则
     */
    public static void setToastHandler(IToastStrategy handler) {
        checkNullPointer(handler);
        sStrategy = handler;
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
     * 生成默认的 TextView 对象
     */
    private static TextView createTextView(Context context) {

        GradientDrawable drawable = new GradientDrawable();
        // 设置背景色
        drawable.setColor(sStyle.getBackgroundColor());
        // 设置圆角大小
        drawable.setCornerRadius(sStyle.getCornerRadius());

        TextView textView = new TextView(context);
        textView.setId(android.R.id.message);
        textView.setTextColor(sStyle.getTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sStyle.getTextSize());

        // 适配布局反方向
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setPaddingRelative(sStyle.getPaddingStart(), sStyle.getPaddingTop(), sStyle.getPaddingEnd(), sStyle.getPaddingBottom());
        } else {
            textView.setPadding(sStyle.getPaddingStart(), sStyle.getPaddingTop(), sStyle.getPaddingEnd(), sStyle.getPaddingBottom());
        }

        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // setBackground API 版本兼容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(drawable);
        } else {
            textView.setBackgroundDrawable(drawable);
        }

        // 设置 Z 轴阴影
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setZ(sStyle.getZ());
        }

        // 设置最大显示行数
        if (sStyle.getMaxLines() > 0) {
            textView.setMaxLines(sStyle.getMaxLines());
        }

        return textView;
    }

    /**
     * 检查通知栏权限有没有开启
     * 参考 SupportCompat 包中的方法： NotificationManagerCompat.from(context).areNotificationsEnabled();
     */
    private static boolean isNotificationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).areNotificationsEnabled();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;

            try {
                Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
                int value = (Integer) opPostNotificationValue.get(Integer.class);
                return ((int) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException | RuntimeException ignored) {
                return true;
            }
        } else {
            return true;
        }
    }
}