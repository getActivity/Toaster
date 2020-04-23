package com.hjq.toast;

import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/12/06
 *    desc   : Toast 显示安全处理
 */
final class SafeHandler extends Handler {

    private Handler mHandler;

    SafeHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void handleMessage(Message msg) {
        // 捕获这个异常，避免程序崩溃
        try {
            // 目前发现在 Android 7.1 主线程被阻塞之后弹吐司会导致崩溃，可使用 Thread.sleep(5000) 进行复现
            // 查看源码得知 Google 已经在 Android 8.0 已经修复了此问题
            // 主线程阻塞之后 Toast 也会被阻塞，Toast 因为超时导致 Window Token 失效
            mHandler.handleMessage(msg);
        } catch (WindowManager.BadTokenException | IllegalStateException ignored) {
            // android.view.WindowManager$BadTokenException：Unable to add window -- token android.os.BinderProxy is not valid; is your activity running?
            // java.lang.IllegalStateException：java.lang.IllegalStateException：View android.widget.LinearLayout has already been added to the window manager.
        }
    }
}