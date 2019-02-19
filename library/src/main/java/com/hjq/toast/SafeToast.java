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

        // Hook toast field
        try {
            Field field_tn = Toast.class.getDeclaredField("mTN");
            field_tn.setAccessible(true);

            Object mTN = field_tn.get(this);
            Field field_handler = field_tn.getType().getDeclaredField("mHandler");
            field_handler.setAccessible(true);

            Handler handler = (Handler) field_handler.get(mTN);
            field_handler.set(mTN, new SafeHandler(handler)); // 偷梁换柱

        } catch (Exception ignored) {}
    }

    static final class SafeHandler extends Handler {

        private Handler mHandler;

        SafeHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            // 捕获这个异常，避免程序崩溃
            try {
                /*
                 目前发现 Android 7.1 主线程被阻塞之后弹吐司会导致崩溃
                 查看源码得知 Google 已经在 8.0 已经修复了此问题
                 因为主线程阻塞之后 Toast 也会被阻塞
                 Toast 超时 Window token 会失效
                 可使用 Thread.sleep(5000) 进行复现
                 */
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException ignored) {
                // android.view.WindowManager$BadTokenException:
                // Unable to add window -- token android.os.BinderProxy@94ae84f is not valid; is your activity running?
            }
        }

        @Override
        public void dispatchMessage(Message msg) {
            mHandler.dispatchMessage(msg);
        }
    }
}