package com.hjq.toast;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;

import com.hjq.toast.config.IToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/02
 *    desc   : 自定义 Toast 实现类
 */
final class ToastImpl {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    /** 短吐司显示的时长 */
    private static final int SHORT_DURATION_TIMEOUT = 2000;
    /** 长吐司显示的时长 */
    private static final int LONG_DURATION_TIMEOUT = 3500;

    /** 当前的吐司对象 */
    private final IToast mToast;

    /** WindowManager 辅助类 */
    private final WindowLifecycle mWindowLifecycle;

    /** 当前应用的包名 */
    private final String mPackageName;

    /** 当前是否已经显示 */
    private boolean mShow;

    ToastImpl(Activity activity, IToast toast) {
        mToast = toast;
        mPackageName = activity.getPackageName();
        mWindowLifecycle = new WindowLifecycle(activity);
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
        HANDLER.removeCallbacks(mShowRunnable);
        HANDLER.post(mShowRunnable);
    }

    /**
     * 取消吐司弹窗
     */
    void cancel() {
        if (!isShow()) {
            return;
        }
        // 移除之前移除吐司的任务
        HANDLER.removeCallbacks(mCancelRunnable);
        HANDLER.post(mCancelRunnable);
    }

    private final Runnable mShowRunnable = new Runnable() {

        @Override
        public void run() {
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = android.R.style.Animation_Toast;
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.packageName = mPackageName;
            params.gravity = mToast.getGravity();
            params.x = mToast.getXOffset();
            params.y = mToast.getYOffset();
            params.verticalMargin = mToast.getVerticalMargin();
            params.horizontalMargin = mToast.getHorizontalMargin();

            try {
                Activity activity = mWindowLifecycle.getActivity();
                if (activity == null || activity.isFinishing()) {
                    return;
                }

                WindowManager manager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                if (manager == null) {
                    return;
                }

                manager.addView(mToast.getView(), params);
                // 添加一个移除吐司的任务
                HANDLER.postDelayed(() -> cancel(), mToast.getDuration() == Toast.LENGTH_LONG ?
                        LONG_DURATION_TIMEOUT : SHORT_DURATION_TIMEOUT);
                // 当前已经显示
                setShow(true);
                // 注册生命周期管控
                mWindowLifecycle.register(ToastImpl.this);

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
                Activity activity = mWindowLifecycle.getActivity();
                if (activity == null) {
                    return;
                }

                WindowManager manager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                if (manager == null) {
                    return;
                }

                manager.removeViewImmediate(mToast.getView());

            } catch (IllegalArgumentException e) {
                // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
                // java.lang.IllegalArgumentException: View=android.widget.TextView not attached to window manager
                e.printStackTrace();
            } finally {
                // 当前没有显示
                setShow(false);
                // 反注册生命周期管控
                mWindowLifecycle.unregister();
            }
        }
    };
}