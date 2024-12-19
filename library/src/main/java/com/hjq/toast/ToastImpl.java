package com.hjq.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2018/11/02
 *    desc   : 自定义 Toast 实现类
 */
final class ToastImpl {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    /** 当前的吐司对象 */
    private final CustomToast mToast;

    /** WindowManager 辅助类 */
    private WindowLifecycle mWindowLifecycle;

    /** 当前应用的包名 */
    private final String mPackageName;

    /** 当前是否已经显示 */
    private boolean mShow;

    /** 当前是否全局显示 */
    private boolean mGlobalShow;

    ToastImpl(Activity activity, CustomToast toast) {
        this((Context) activity, toast);
        mGlobalShow = false;
        mWindowLifecycle = new WindowLifecycle(activity);
    }

    ToastImpl(Application application, CustomToast toast) {
        this((Context) application, toast);
        mGlobalShow = true;
        mWindowLifecycle = new WindowLifecycle(application);
    }

    private ToastImpl(Context context, CustomToast toast) {
        mToast = toast;
        mPackageName = context.getPackageName();
    }

    boolean isShow() {
        return mShow;
    }

    void setShow(boolean show) {
        mShow = show;
    }

    /***
     * 显示吐司弹窗
     */
    void show() {
        if (isShow()) {
            return;
        }
        if (isMainThread()) {
            mShowRunnable.run();
        } else {
            HANDLER.removeCallbacks(mShowRunnable);
            HANDLER.post(mShowRunnable);
        }
    }

    /**
     * 取消吐司弹窗
     */
    void cancel() {
        if (!isShow()) {
            return;
        }
        HANDLER.removeCallbacks(mShowRunnable);
        if (isMainThread()) {
            mCancelRunnable.run();
        } else {
            HANDLER.removeCallbacks(mCancelRunnable);
            HANDLER.post(mCancelRunnable);
        }
    }

    /**
     * 判断当前是否在主线程
     */
    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 发送无障碍事件
     */
    @SuppressWarnings("deprecation")
    private void sendAccessibilityEvent(View view) {
        final Context context = view.getContext();
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }
        int eventType = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        AccessibilityEvent event;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            event = new AccessibilityEvent();
            event.setEventType(eventType);
        } else {
            event = AccessibilityEvent.obtain(eventType);
        }
        event.setClassName(Toast.class.getName());
        event.setPackageName(context.getPackageName());
        view.dispatchPopulateAccessibilityEvent(event);
        // 将 Toast 视为通知，因为它们用于向用户宣布短暂的信息
        accessibilityManager.sendAccessibilityEvent(event);
    }

    private final Runnable mShowRunnable = new Runnable() {

        @SuppressLint("WrongConstant")
        @Override
        public void run() {
            
            WindowManager windowManager = mWindowLifecycle.getWindowManager();
            if (windowManager == null) {
                return;
            }

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.packageName = mPackageName;
            params.gravity = mToast.getGravity();
            params.x = mToast.getXOffset();
            params.y = mToast.getYOffset();
            params.verticalMargin = mToast.getVerticalMargin();
            params.horizontalMargin = mToast.getHorizontalMargin();
            params.windowAnimations = mToast.getAnimationsId();

            // 指定 WindowManager 忽略系统窗口可见性的影响
            // 例如下面这些的显示和隐藏都会影响当前 WindowManager 的显示（触发位置调整）
            // WindowInsets.Type.statusBars()：状态栏
            // WindowInsets.Type.navigationBars()：导航栏
            // WindowInsets.Type.ime()：输入法（软键盘）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                params.setFitInsetsIgnoringVisibility(true);
            }

            // 如果是全局显示
            if (mGlobalShow) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    // 在 type 等于 TYPE_APPLICATION_OVERLAY 的时候
                    // 不能添加 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 标记
                    // 否则会导致在 Android 13 上面会出现 Toast 布局被半透明化的效果
                    // Github issue 地址：https://github.com/getActivity/Toaster/issues/108
                    params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                } else {
                    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
            }

            try {
                windowManager.addView(mToast.getView(), params);
                // 添加一个移除吐司的任务
                HANDLER.postDelayed(() -> cancel(), mToast.getDuration() == Toast.LENGTH_LONG ?
                        mToast.getLongDuration() : mToast.getShortDuration());
                // 注册生命周期管控
                mWindowLifecycle.register(ToastImpl.this);
                // 当前已经显示
                setShow(true);
                // 发送无障碍事件
                sendAccessibilityEvent(mToast.getView());
            } catch (IllegalStateException | WindowManager.BadTokenException e) {
                // 如果这个 View 对象被重复添加到 WindowManager 则会抛出异常
                // java.lang.IllegalStateException: View android.widget.TextView has already been added to the window manager.
                // 如果 WindowManager 绑定的 Activity 已经销毁，则会抛出异常
                // android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@ef1ccb6 is not valid; is your activity running?
                e.printStackTrace();
            }
        }
    };

    private final Runnable mCancelRunnable = new Runnable() {

        @Override
        public void run() {

            try {
                WindowManager windowManager = mWindowLifecycle.getWindowManager();
                if (windowManager == null) {
                    return;
                }

                windowManager.removeViewImmediate(mToast.getView());

            } catch (IllegalArgumentException e) {
                // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
                // java.lang.IllegalArgumentException: View=android.widget.TextView not attached to window manager
                e.printStackTrace();
            } finally {
                // 反注册生命周期管控
                mWindowLifecycle.unregister();
                // 当前没有显示
                setShow(false);
            }
        }
    };
}