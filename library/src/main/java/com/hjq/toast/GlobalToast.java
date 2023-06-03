package com.hjq.toast;

import android.app.Application;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2021/11/30
 *    desc   : 利用悬浮窗权限弹出全局 Toast
 */
public class GlobalToast extends CustomToast {

    /** Toast 实现类 */
    private final ToastImpl mToastImpl;

    public GlobalToast(Application application) {
        mToastImpl = new ToastImpl(application, this);
    }

    @Override
    public void show() {
        // 替换成 WindowManager 来显示
        mToastImpl.show();
    }

    @Override
    public void cancel() {
        // 取消 WindowManager 的显示
        mToastImpl.cancel();
    }
}