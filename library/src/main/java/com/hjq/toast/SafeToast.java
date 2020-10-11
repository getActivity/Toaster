package com.hjq.toast;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/12/06
 *    desc   : Toast 崩溃处理
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public final class SafeToast extends NormalToast {

    public SafeToast(Application application) {
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

        } catch (IllegalAccessException | NoSuchFieldException ignored) {}
    }
}