package com.hjq.toast;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import com.hjq.toast.config.IToast;
import com.hjq.toast.config.IToastStrategy;
import com.hjq.toast.config.IToastStyle;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/12
 *    desc   : Toast 默认处理器
 *    doc    : https://developer.android.google.cn/reference/android/widget/Toast
 */
public class ToastStrategy implements IToastStrategy {

    /** Handler 对象 */
    private final static Handler HANDLER = new Handler(Looper.getMainLooper());

    /** 延迟时间 */
    private static final int DELAY_TIMEOUT = 200;

    /** 应用上下文 */
    private Application mApplication;

    /** Activity 栈管理 */
    private ActivityStack mActivityStack;

    /** Toast 对象 */
    private WeakReference<IToast> mToastReference;

    /** Toast 样式 */
    private IToastStyle<?> mToastStyle;

    /** 最新的文本 */
    private volatile CharSequence mLatestText;

    @Override
    public void registerStrategy(Application application) {
        mApplication = application;
        mActivityStack = ActivityStack.register(application);
    }

    @Override
    public void bindStyle(IToastStyle<?> style) {
        mToastStyle = style;
    }

    @Override
    public IToast createToast(Application application) {
        Activity foregroundActivity = mActivityStack.getForegroundActivity();
        IToast toast;
        if (foregroundActivity != null) {
            toast = new ActivityToast(foregroundActivity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(application)) {
            // 如果有悬浮窗权限，就开启全局的 Toast
            toast = new WindowToast(application);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            // 处理 Android 7.1 上 Toast 在主线程被阻塞后会导致报错的问题
            toast = new SafeToast(application);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !areNotificationsEnabled(application)) {
            // 处理 Toast 关闭通知栏权限之后无法弹出的问题
            // 通过查看和对比 NotificationManagerService 的源码
            // 发现这个问题已经在 Android 10 版本上面修复了
            // 但是 Toast 只能在前台显示，没有通知栏权限后台 Toast 仍然无法显示
            // 并且 Android 10 刚好禁止了 Hook 通知服务
            // 已经有通知栏权限，不需要 Hook 系统通知服务也能正常显示系统 Toast
            toast = new NotificationToast(application);
        } else {
            toast = new SystemToast(application);
        }

        // targetSdkVersion >= 30 的情况下在后台显示自定义样式的 Toast 会被系统屏蔽，并且日志会输出以下警告：
        // Blocking custom toast from package com.xxx.xxx due to package not in the foreground
        // targetSdkVersion < 30 的情况下 new Toast，并且不设置视图显示，系统会抛出以下异常：
        // java.lang.RuntimeException: This Toast was not created with Toast.makeText()
        if (toast instanceof CustomToast || Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                application.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.R) {
            toast.setView(mToastStyle.createView(application));
            toast.setGravity(mToastStyle.getGravity(), mToastStyle.getXOffset(), mToastStyle.getYOffset());
            toast.setMargin(mToastStyle.getHorizontalMargin(), mToastStyle.getVerticalMargin());
        }
        return toast;
    }

    @Override
    public void showToast(CharSequence text, long delayMillis) {
        mLatestText = text;
        HANDLER.removeCallbacks(mShowRunnable);
        // 延迟一段时间之后再执行，因为在没有通知栏权限的情况下，Toast 只能显示当前 Activity
        // 如果当前 Activity 在 ToastUtils.show 之后进行 finish 了，那么这个时候 Toast 可能会显示不出来
        // 因为 Toast 会显示在销毁 Activity 界面上，而不会显示在新跳转的 Activity 上面
        HANDLER.postDelayed(mShowRunnable, delayMillis + DELAY_TIMEOUT);
    }

    @Override
    public void cancelToast() {
        HANDLER.removeCallbacks(mCancelRunnable);
        HANDLER.post(mCancelRunnable);
    }

    /**
     * 显示任务
     */
    private final Runnable mShowRunnable = new Runnable() {

        @Override
        public void run() {
            IToast toast = null;
            if (mToastReference != null) {
                toast = mToastReference.get();
            }

            if (toast != null) {
                // 取消上一个 Toast 的显示
                toast.cancel();
            }

            toast = createToast(mApplication);
            // 为什么用 WeakReference，而不用 SoftReference ？
            // https://github.com/getActivity/ToastUtils/issues/79
            mToastReference = new WeakReference<>(toast);
            toast.setDuration(getToastDuration(mLatestText));
            toast.setText(mLatestText);
            toast.show();
        }
    };

    /**
     * 取消任务
     */
    private final Runnable mCancelRunnable = new Runnable() {

        @Override
        public void run() {
            IToast toast = null;
            if (mToastReference != null) {
                toast = mToastReference.get();
            }

            if (toast == null) {
                return;
            }
            toast.cancel();
        }
    };

    /**
     * 获取 Toast 显示时长
     */
    protected int getToastDuration(CharSequence text) {
        return text.length() > 20 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
    }

    /**
     * 是否有通知栏权限
     */
    @SuppressWarnings("ConstantConditions")
    protected boolean areNotificationsEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getSystemService(NotificationManager.class).areNotificationsEnabled();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 参考 Support 库中的方法： NotificationManagerCompat.from(context).areNotificationsEnabled()
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = appOps.getClass().getMethod("checkOpNoThrow",
                        Integer.TYPE, Integer.TYPE, String.class);
                Field field = appOps.getClass().getDeclaredField("OP_POST_NOTIFICATION");
                int value = (int) field.get(Integer.class);
                return ((int) method.invoke(appOps, value, context.getApplicationInfo().uid,
                        context.getPackageName())) == AppOpsManager.MODE_ALLOWED;
            } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException |
                    IllegalAccessException | RuntimeException e) {
                e.printStackTrace();
                return true;
            }
        }
        return true;
    }
}