package com.hjq.toast;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/06
 *    desc   : WindowManager 辅助类（用于获取当前 Activity 的 WindowManager 对象）
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
final class WindowHelper implements Application.ActivityLifecycleCallbacks {

    /** 栈顶 Activity */
    private Activity mTopActivity;

    /** 用于 Activity 暂停时移除 WindowManager */
    private final ToastHelper mToastHelper;

    private WindowHelper(ToastHelper toast) {
        mToastHelper = toast;
    }

    static WindowHelper register(ToastHelper toast, Application application) {
        WindowHelper window = new WindowHelper(toast);
        application.registerActivityLifecycleCallbacks(window);
        return window;
    }

    /**
     * 获取栈顶的 Activity
     */
    Activity getTopActivity() {
        return mTopActivity;
    }

    /**
     * {@link Application.ActivityLifecycleCallbacks}
     */

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mTopActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mTopActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mTopActivity = activity;
    }

    // A 跳转 B 页面的生命周期方法执行顺序：
    // onPause(A) ---> onCreate(B) ---> onStart(B) ---> onResume(B) ---> onStop(A) ---> onDestroyed(A)

    @Override
    public void onActivityPaused(Activity activity) {
        // 取消这个吐司的显示
        if (mToastHelper.isShow()) {
            // 不能放在 onStop 或者 onDestroyed 方法中，因为此时新的 Activity 已经创建完成，必须在这个新的 Activity 未创建之前关闭这个 WindowManager
            // 调用取消显示会直接导致新的 Activity 的 onCreate 调用显示吐司可能显示不出来的问题，又或者有时候会立马显示然后立马消失的那种效果
            mToastHelper.cancel();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (mTopActivity == activity) {
            // 移除对这个 Activity 的引用
            mTopActivity = null;
        }
    }
}