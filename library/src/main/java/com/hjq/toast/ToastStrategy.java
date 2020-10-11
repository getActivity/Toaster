package com.hjq.toast;

import android.app.AppOpsManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

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
    /** 继续显示 */
    private static final int TYPE_CONTINUE = 2;
    /** 取消显示 */
    private static final int TYPE_CANCEL = 3;

    /** 队列最大容量 */
    private static final int MAX_TOAST_CAPACITY = 3;

    /** 吐司队列 */
    private volatile Queue<CharSequence> mQueue;

    /** 当前是否正在执行显示操作 */
    private volatile boolean mShow;

    /** 吐司对象 */
    private Toast mToast;

    public ToastStrategy() {
        super(Looper.getMainLooper());
        mQueue = getToastQueue();
    }

    @Override
    public Toast create(Application application) {
        Toast toast;
        // 初始化吐司
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 适配 Android 11 无法使用自定义 Toast 的问题
            // 官方文档：https://developer.android.google.cn/preview/features/toasts
            toast = new CustomToast(application);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            // 处理 Android 7.1 上 Toast 在主线程被阻塞后会导致报错的问题
            toast = new SafeToast(application);
        } else {
            boolean check =
                    // 对比不同版本的 NMS 的源码发现这个问题在 Android 9.0 已经被谷歌修复了
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                            // 判断当前应用是否有通知栏权限，如果关闭会导致弹 Toast 无法显示
                            areNotificationsEnabled(application) ||
                            // 判断当前是否是小米手机，因为只有小米手机做了特殊处理，就算没有通知栏权限也能弹吐司
                            "xiaomi".equals(Build.MANUFACTURER.toLowerCase());
            if (check) {
                // 检查通过，返回正常类型的 Toast 即可
                toast = new NormalToast(application);
            } else {
                // 修复关闭通知栏权限后 Toast 不显示的问题
                toast = new CustomToast(application);
            }
        }
        return toast;
    }

    @Override
    public void bind(Toast toast) {
        mToast = toast;
    }

    @Override
    public void show(CharSequence text) {
        if (mQueue.isEmpty() || !mQueue.contains(text)) {
            // 添加一个元素并返回true，如果队列已满，则返回false
            if (!mQueue.offer(text)) {
                // 移除队列头部元素并添加一个新的元素
                mQueue.poll();
                mQueue.offer(text);
            }
        }

        if (!mShow) {
            mShow = true;
            // 延迟一段时间之后再执行，因为在没有通知栏权限的情况下，Toast 只能显示当前 Activity
            // 如果当前 Activity 在 ToastUtils.show 之后进行 finish 了，那么这个时候 Toast 可能会显示不出来
            // 因为 Toast 会显示在销毁 Activity 界面上，而不会显示在新跳转的 Activity 上面
            sendEmptyMessageDelayed(TYPE_SHOW, DELAY_TIMEOUT);
        }
    }

    @Override
    public void cancel() {
        if (mShow) {
            mShow = false;
            sendEmptyMessage(TYPE_CANCEL);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case TYPE_SHOW:
                // 返回队列头部的元素，如果队列为空，则返回null
                CharSequence text = mQueue.peek();
                if (text != null) {
                    mToast.setText(text);
                    mToast.show();
                    // 等这个 Toast 显示完后再继续显示，要加上一些延迟
                    // 不然在某些手机上 Toast 可能会来不及消失就要进行显示，这样是显示不出来的
                    sendEmptyMessageDelayed(TYPE_CONTINUE, getToastDuration(text) + DELAY_TIMEOUT);
                } else {
                    mShow = false;
                }
                break;
            case TYPE_CONTINUE:
                // 移除并返问队列头部的元素，如果队列为空，则返回null
                mQueue.poll();
                if (!mQueue.isEmpty()) {
                    sendEmptyMessage(TYPE_SHOW);
                } else {
                    mShow = false;
                }
                break;
            case TYPE_CANCEL:
                mShow = false;
                mQueue.clear();
                mToast.cancel();
                break;
            default:
                break;
        }
    }

    /**
     * 获取吐司队列
     */
    public Queue<CharSequence> getToastQueue() {
        return new ArrayBlockingQueue<>(MAX_TOAST_CAPACITY);
    }

    /**
     * 根据文本来获取吐司的显示时长
     */
    public int getToastDuration (CharSequence text) {
        // 如果显示的文字超过了 20 个字符就显示长吐司，否则显示短吐司
        return text.length() > 20 ? LONG_DURATION_TIMEOUT : SHORT_DURATION_TIMEOUT;
    }

    /**
     * 检查通知栏权限有没有开启
     *
     * 参考 SupportCompat 包中的方法： NotificationManagerCompat.from(context).areNotificationsEnabled();
     */
    private static boolean areNotificationsEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getSystemService(NotificationManager.class).areNotificationsEnabled();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = appOps.getClass().getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);
                Field field = appOps.getClass().getDeclaredField("OP_POST_NOTIFICATION");
                int value = (Integer) field.get(Integer.class);
                return ((int) method.invoke(appOps, value, context.getApplicationInfo().uid, context.getPackageName())) == AppOpsManager.MODE_ALLOWED;
            } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException | RuntimeException ignored) {
                return true;
            }
        } else {
            return true;
        }
    }
}