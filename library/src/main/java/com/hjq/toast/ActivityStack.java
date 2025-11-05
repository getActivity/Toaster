package com.hjq.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2021/04/07
 *    desc   : Activity 生命周期监控
 */
final class ActivityStack implements Application.ActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static volatile ActivityStack sInstance;

    public static ActivityStack getInstance() {
        if(sInstance == null) {
            synchronized (ActivityStack.class) {
                if (sInstance == null) {
                    sInstance = new ActivityStack();
                }
            }
        }
        return sInstance;
    }

    /** 私有化构造函数 */
    private ActivityStack() {}

    /**
     * 注册 Activity 生命周期监听
     */
    public void register(Application application) {
        if (application == null) {
            return;
        }
        application.registerActivityLifecycleCallbacks(this);
    }

    /** 当前焦点的 Activity 对象 */
    private Activity mFocusActivity;

    /** 当前可见的 Activity 对象 */
    private Activity mVisibleActivity;

    /** Activity 可见时候的时间戳 */
    private long mActivityResumedTime;

    public Activity getFocusActivity() {
        return mFocusActivity;
    }

    public Activity getVisibleActivity() {
        return mVisibleActivity;
    }

    public long getActivityResumedTime() {
        return mActivityResumedTime;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        mFocusActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mVisibleActivity = activity;
        mActivityResumedTime = System.currentTimeMillis();
        // 这里解释一下为什么要在 Resumed 时给 FocusActivity 对象赋值？
        // 这是因为有人反馈在跳转到新的 Activity 后又立马销毁的情况下，无法显示自定义样式的 Toast，
        // 经过排查发现，在这个过程 Activity 会回调 Paused 生命周期，然后直接回调 Resumed 生命周期，
        // 这样就会导致 Started 生命周期没有被回调，这样就导致 FocusActivity 对象会为 null，
        // 为了处理这种情况，最好的方式就是在 Resumed 生命周期中再检查一下 FocusActivity 对象是否为空，
        // 如果 FocusActivity 对象为空，则直接赋值为 Resumed 生命周期时的 Activity 对象。
        // Github 地址：https://github.com/getActivity/Toaster/issues/157
        if (mFocusActivity != null) {
            return;
        }
        mFocusActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mFocusActivity != activity) {
            return;
        }
        mFocusActivity = null;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (mVisibleActivity != activity) {
            return;
        }
        mVisibleActivity = null;
        mActivityResumedTime = 0;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}