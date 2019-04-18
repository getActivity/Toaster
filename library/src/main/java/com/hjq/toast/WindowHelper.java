package com.hjq.toast;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.WindowManager;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/06
 *    desc   : WindowManager 辅助类（用于获取当前 Activity 的 WindowManager 对象）
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
final class WindowHelper implements Application.ActivityLifecycleCallbacks {

    // Activity 存放集合
    private final ArrayMap<String, Activity> mActivitySet = new ArrayMap<>();

    // 用于 Activity 暂停时移除 WindowManager
    private final ToastHelper mToastHelper;

    // 当前 Activity 对象标记
    private String mCurrentTag;

    private WindowHelper(ToastHelper toast) {
        mToastHelper = toast;
    }

    static WindowHelper register(ToastHelper toast, Application application) {
        WindowHelper window = new WindowHelper(toast);
        application.registerActivityLifecycleCallbacks(window);
        return window;
    }

    /**
     * 获取一个WindowManager对象
     *
     * @return          如果获取不到则抛出空指针异常
     */
    WindowManager getWindowManager() throws NullPointerException {
        if (mCurrentTag != null) {
            // 如果使用的 WindowManager 对象不是当前 Activity 创建的，则会抛出异常
            // android.view.WindowManager$BadTokenException: Unable to add window -- token null is not for an application
            Activity activity = mActivitySet.get(mCurrentTag);
            if (activity != null) {
                return getWindowManagerObject(activity);
            }
        }
        throw new NullPointerException();
    }

    /**
     * {@link Application.ActivityLifecycleCallbacks}
     */

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mCurrentTag = getObjectTag(activity);
        mActivitySet.put(mCurrentTag, activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mCurrentTag = getObjectTag(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mCurrentTag = getObjectTag(activity);
    }

    // A 跳转 B 页面的生命周期方法执行顺序：
    // onPause(A) ---> onCreate(B) ---> onStart(B) ---> onResume(B) ---> onStop(A) ---> onDestroyed(A)

    @Override
    public void onActivityPaused(Activity activity) {
        // 取消这个吐司的显示
        mToastHelper.cancel();
        // 不能放在 onStop 或者 onDestroyed 方法中，因为此时新的 Activity 已经创建完成，必须在这个新的 Activity 未创建之前关闭这个 WindowManager
        // 调用取消显示会直接导致新的 Activity 的 onCreate 调用显示吐司可能显示不出来的问题（又或者有时候会立马显示然后立马消失的那种效果）
    }

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        // 移除对这个 Activity 的引用
        mActivitySet.remove(getObjectTag(activity));
        // 如果当前的 Activity 是最后一个的话
        if (getObjectTag(activity).equals(mCurrentTag)) {
            // 清除当前标记
            mCurrentTag = null;
        }
    }

    /**
     * 获取一个对象的独一无二的标记
     */
    private static String getObjectTag(Object object) {
        // 对象所在的包名 + 对象的内存地址
        return object.getClass().getName() + Integer.toHexString(object.hashCode());
    }

    /**
     * 获取一个 WindowManager 对象
     */
    private static WindowManager getWindowManagerObject(Activity activity) {
        return ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE));
    }
}