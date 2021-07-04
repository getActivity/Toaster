package com.hjq.toast;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.hjq.toast.config.IToast;
import com.hjq.toast.config.IToastStrategy;
import com.hjq.toast.config.IToastStyle;

import java.lang.ref.WeakReference;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/12
 *    desc   : Toast 默认处理器
 */
public class ToastStrategy extends Handler implements IToastStrategy {

    /** 延迟时间 */
    private static final int DELAY_TIMEOUT = 200;

    /** 显示吐司 */
    private static final int TYPE_SHOW = 1;
    /** 取消显示 */
    private static final int TYPE_CANCEL = 2;

    /** 应用上下文 */
    private Application mApplication;

    /** Activity 栈管理 */
    private ActivityStack mActivityStack;

    /** Toast 对象 */
    private WeakReference<IToast> mToastReference;

    /** Toast 样式 */
    private IToastStyle<?> mToastStyle;

    public ToastStrategy() {
        super(Looper.getMainLooper());
    }

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
        Activity resumedActivity = mActivityStack.getForegroundActivity();
        IToast toast;
        if (resumedActivity != null) {
            toast = new ActivityToast(resumedActivity);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            // 处理 Android 7.1 上 Toast 在主线程被阻塞后会导致报错的问题
            toast = new SafeToast(application);
        } else {
            toast = new SystemToast(application);
        }

        // targetSdkVersion >= 30 的情况下在后台显示自定义样式的 Toast 会被系统屏蔽，并且日志会输出以下警告：
        // Blocking custom toast from package com.xxx.xxx due to package not in the foreground
        // targetSdkVersion < 30 的情况下 new Toast，并且不设置样式显示，系统会抛出以下异常：
        // java.lang.RuntimeException: This Toast was not created with Toast.makeText()
        if (toast instanceof ActivityToast || Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                application.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.R) {
            toast.setView(mToastStyle.createView(application));
            toast.setGravity(mToastStyle.getGravity(), mToastStyle.getXOffset(), mToastStyle.getYOffset());
            toast.setMargin(mToastStyle.getHorizontalMargin(), mToastStyle.getVerticalMargin());
        }
        return toast;
    }

    @Override
    public void showToast(CharSequence text) {
        removeMessages(TYPE_SHOW);
        // 延迟一段时间之后再执行，因为在没有通知栏权限的情况下，Toast 只能显示当前 Activity
        // 如果当前 Activity 在 ToastUtils.show 之后进行 finish 了，那么这个时候 Toast 可能会显示不出来
        // 因为 Toast 会显示在销毁 Activity 界面上，而不会显示在新跳转的 Activity 上面
        Message msg = Message.obtain();
        msg.what = TYPE_SHOW;
        msg.obj = text;
        sendMessageDelayed(msg, DELAY_TIMEOUT);
    }

    @Override
    public void cancelToast() {
        removeMessages(TYPE_CANCEL);
        sendEmptyMessage(TYPE_CANCEL);
    }

    @Override
    public void handleMessage(Message msg) {
        IToast toast = null;
        if (mToastReference != null) {
            toast = mToastReference.get();
        }

        switch (msg.what) {
            case TYPE_SHOW:
                // 返回队列头部的元素，如果队列为空，则返回 null
                if (!(msg.obj instanceof CharSequence)) {
                    break;
                }

                CharSequence text = (CharSequence) msg.obj;

                if (toast != null) {
                    // 取消上一个 Toast 的显示
                    toast.cancel();
                }

                toast = createToast(mApplication);
                mToastReference = new WeakReference<>(toast);
                toast.setDuration(getToastDuration(text));
                toast.setText(text);
                toast.show();
                break;
            case TYPE_CANCEL:
                if (toast == null) {
                    break;
                }
                toast.cancel();
                break;
            default:
                break;
        }
    }

    /**
     * 获取 Toast 显示时长
     */
    protected int getToastDuration(CharSequence text) {
        return text.length() > 20 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
    }
}