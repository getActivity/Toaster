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