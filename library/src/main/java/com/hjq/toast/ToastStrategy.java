package com.hjq.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2018/11/12
 *    desc   : Toast 默认处理器
 *    doc    : https://developer.android.google.cn/reference/android/widget/Toast
 */
public class ToastStrategy implements IToastStrategy {

    /**
     * 即显即示模式（默认）
     *
     * 在发起多次 Toast 的显示请求情况下，显示下一个 Toast 之前
     * 会先立即取消上一个 Toast，保证当前显示 Toast 消息是最新的
     */
    public static final int SHOW_STRATEGY_TYPE_IMMEDIATELY = 0;

    /**
     * 不丢消息模式
     *
     * 在发起多次 Toast 的显示请求情况下，等待上一个 Toast 显示 1 秒或者 1.5 秒后
     * 然后再显示下一个 Toast，不按照 Toast 的显示时长来，因为那样等待时间会很长
     * 这样既能保证用户能看到每一条 Toast 消息，又能保证用户不会等得太久，速战速决
     */
    public static final int SHOW_STRATEGY_TYPE_QUEUE = 1;

    /** Handler 对象 */
    private final static Handler HANDLER = new Handler(Looper.getMainLooper());

    /**
     * 默认延迟时间
     *
     * 延迟一段时间之后再执行，因为在没有通知栏权限的情况下，Toast 只能显示在当前 Activity 上面
     * 如果当前 Activity 在 showToast 之后立马进行 finish 了，那么这个时候 Toast 可能会显示不出来
     * 因为 Toast 会显示在销毁 Activity 界面上，而不会显示在新跳转的 Activity 上面
     */
    private static final int DEFAULT_DELAY_TIMEOUT = 200;

    /** 应用上下文 */
    private Application mApplication;

    /** Toast 对象 */
    private WeakReference<IToast> mToastReference;

    /** 吐司显示策略 */
    private final int mShowStrategyType;

    /** 显示消息 Token */
    private final Object mShowMessageToken = new Object();
    /** 取消消息 Token */
    private final Object mCancelMessageToken = new Object();

    /** 上一个 Toast 显示的时间 */
    private volatile long mLastShowToastMillis;

    public ToastStrategy() {
        this(ToastStrategy.SHOW_STRATEGY_TYPE_IMMEDIATELY);
    }

    public ToastStrategy(int type) {
        mShowStrategyType = type;
        switch (mShowStrategyType) {
            case SHOW_STRATEGY_TYPE_IMMEDIATELY:
            case SHOW_STRATEGY_TYPE_QUEUE:
                break;
            default:
                throw new IllegalArgumentException("Please don't pass non-existent toast show strategy");
        }
    }

    @Override
    public void registerStrategy(Application application) {
        mApplication = application;
        ActivityStack.getInstance().register(application);
    }

    @Override
    public IToast createToast(IToastStyle<?> style) {
        Activity foregroundActivity = ActivityStack.getInstance().getForegroundActivity();
        IToast toast;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(mApplication)) {
            // 如果有悬浮窗权限，就开启全局的 Toast
            toast = new GlobalToast(mApplication);
        } else if (foregroundActivity != null) {
            // 如果没有悬浮窗权限，就开启一个依附于 Activity 的 Toast
            toast = new ActivityToast(foregroundActivity);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            // 处理 Android 7.1 上 Toast 在主线程被阻塞后会导致报错的问题
            toast = new SafeToast(mApplication);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !areNotificationsEnabled(mApplication)) {
            // 处理 Toast 关闭通知栏权限之后无法弹出的问题
            // 通过查看和对比 NotificationManagerService 的源码
            // 发现这个问题已经在 Android 10 版本上面修复了
            // 但是 Toast 只能在前台显示，没有通知栏权限后台 Toast 仍然无法显示
            // 并且 Android 10 刚好禁止了 Hook 通知服务
            // 已经有通知栏权限，不需要 Hook 系统通知服务也能正常显示系统 Toast
            toast = new NotificationToast(mApplication);
        } else {
            toast = new SystemToast(mApplication);
        }
        if (isSupportToastStyle(toast) || !onlyShowSystemToastStyle()) {
            diyToastStyle(toast, style);
        }
        return toast;
    }

    @Override
    public void showToast(ToastParams params) {
        switch (mShowStrategyType) {
            case SHOW_STRATEGY_TYPE_IMMEDIATELY: {
                // 移除之前未显示的 Toast 消息
                HANDLER.removeCallbacksAndMessages(mShowMessageToken);
                long uptimeMillis = SystemClock.uptimeMillis() + params.delayMillis + DEFAULT_DELAY_TIMEOUT;
                HANDLER.postAtTime(new ShowToastRunnable(params), mShowMessageToken, uptimeMillis);
                break;
            }
            case SHOW_STRATEGY_TYPE_QUEUE: {
                // 计算出这个 Toast 显示时间
                long showToastMillis = SystemClock.uptimeMillis() + params.delayMillis + DEFAULT_DELAY_TIMEOUT;
                // 根据吐司的长短计算出等待时间
                long waitMillis = generateToastWaitMillis(params);
                // 如果当前显示的时间在上一个 Toast 的显示范围之内
                // 那么就重新计算 Toast 的显示时间
                if (showToastMillis < (mLastShowToastMillis + waitMillis)) {
                    showToastMillis = mLastShowToastMillis + waitMillis;
                }
                HANDLER.postAtTime(new ShowToastRunnable(params), mShowMessageToken, showToastMillis);
                mLastShowToastMillis = showToastMillis;
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void cancelToast() {
        HANDLER.removeCallbacksAndMessages(mCancelMessageToken);
        long uptimeMillis = SystemClock.uptimeMillis();
        HANDLER.postAtTime(new CancelToastRunnable(), mCancelMessageToken, uptimeMillis);
    }

    /**
     * 是否支持设置自定义 Toast 样式
     */
    protected boolean isSupportToastStyle(IToast toast) {
        // targetSdkVersion >= 30 的情况下在后台显示自定义样式的 Toast 会被系统屏蔽，并且日志会输出以下警告：
        // Blocking custom toast from package com.xxx.xxx due to package not in the foreground
        // targetSdkVersion < 30 的情况下 new Toast，并且不设置视图显示，系统会抛出以下异常：
        // java.lang.RuntimeException: This Toast was not created with Toast.makeText()
        return toast instanceof CustomToast || Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                mApplication.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.R;
    }

    /**
     * 定制 Toast 的样式
     */
    protected void diyToastStyle(IToast toast, IToastStyle<?> style) {
        toast.setView(style.createView(mApplication));
        toast.setGravity(style.getGravity(), style.getXOffset(), style.getYOffset());
        toast.setMargin(style.getHorizontalMargin(), style.getVerticalMargin());
    }

    /**
     * 生成 Toast 等待时间
     */
    protected int generateToastWaitMillis(ToastParams params) {
        if (params.duration == Toast.LENGTH_SHORT) {
            return 1000;
        } else if (params.duration == Toast.LENGTH_LONG) {
            return 1500;
        }
        return 0;
    }

    /**
     * 显示任务
     */
    private class ShowToastRunnable implements Runnable {

        private final ToastParams mToastParams;

        private ShowToastRunnable(ToastParams params) {
            mToastParams = params;
        }

        @Override
        public void run() {
            IToast toast = null;
            if (mToastReference != null) {
                toast = mToastReference.get();
            }

            if (toast != null) {
                // 取消上一个 Toast 的显示，避免出现重叠的效果
                toast.cancel();
            }
            toast = createToast(mToastParams.style);
            // 为什么用 WeakReference，而不用 SoftReference ？
            // https://github.com/getActivity/Toaster/issues/79
            mToastReference = new WeakReference<>(toast);
            toast.setDuration(mToastParams.duration);
            toast.setText(mToastParams.text);
            toast.show();
        }
    }

    /**
     * 取消任务
     */
    private class CancelToastRunnable implements Runnable {

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
    }

    /**
     * 当前是否只能显示系统 Toast 样式
     */
    protected boolean onlyShowSystemToastStyle() {
        // Github issue 地址：https://github.com/getActivity/Toaster/issues/103
        // Toast.CHANGE_TEXT_TOASTS_IN_THE_SYSTEM = 147798919L
        return isChangeEnabledCompat(147798919L);
    }

    @SuppressLint("PrivateApi")
    protected boolean isChangeEnabledCompat(long changeId) {
        // 需要注意的是这个 api 是在 android 11 的时候出现的，反射前需要先判断好版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return true;
        }
        try {
            // 因为 Compatibility.isChangeEnabled() 普通应用根本调用不到，反射也不行
            // 通过 Toast.isSystemRenderedTextToast 也没有办法反射到
            // 最后发现反射 CompatChanges.isChangeEnabled 是可以的
            Class<?> aClass = Class.forName("android.app.compat.CompatChanges");
            Method method = aClass.getMethod("isChangeEnabled", long.class);
            method.setAccessible(true);
            return Boolean.parseBoolean(String.valueOf(method.invoke(null, changeId)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否有通知栏权限
     */
    @SuppressWarnings("ConstantConditions")
    @SuppressLint("PrivateApi")
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