package com.hjq.toast;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/12/06
 *    desc   : Toast 显示安全处理
 */
final class SafeToast extends BaseToast {

    SafeToast(Application application) {
        super(application);

        // 反射 Toast 中的字段
        try {
            // 获取 mTN 字段对象
            Field mTNField = Toast.class.getDeclaredField("mTN");
            mTNField.setAccessible(true);
            Object mTN = mTNField.get(this);

            // 获取 mTN 中的 mHandler 字段对象
            Field mHandlerField = mTNField.getType().getDeclaredField("mHandler");
            mHandlerField.setAccessible(true);
            Handler mHandler = (Handler) mHandlerField.get(mTN);

            // 偷梁换柱
            mHandlerField.set(mTN, new SafeHandler(mHandler));

        } catch (Exception ignored) {}
    }

    private static final class SafeHandler extends Handler {

        private Handler mHandler;

        private SafeHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            // 捕获这个异常，避免程序崩溃
            try {
                /*
                 目前发现在 Android 7.1 主线程被阻塞之后弹吐司会导致崩溃，可使用 Thread.sleep(5000) 进行复现
                 查看源码得知 Google 已经在 Android 8.0 已经修复了此问题
                 主线程阻塞之后 Toast 也会被阻塞，Toast 因为超时导致 Window Token 失效
                 */
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException ignored) {
                // android.view.WindowManager$BadTokenException:
                // Unable to add window -- token android.os.BinderProxy is not valid; is your activity running?
            }
        }
    }
}