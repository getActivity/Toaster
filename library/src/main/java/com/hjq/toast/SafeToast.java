package com.hjq.toast;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.lang.reflect.Field;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2018/12/06
 *    desc   : Toast 显示安全处理
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@SuppressWarnings("all")
public class SafeToast extends NotificationToast {

    {
        hookToastTN();
    }

    public SafeToast(@NonNull Application application) {
        super(application);
    }

    private void hookToastTN() {
        try {
            // 获取 Toast.mTN 字段对象
            Field tnField = Toast.class.getDeclaredField("mTN");
            tnField.setAccessible(true);
            Object tnObject = tnField.get(this);

            // 获取 mTN 中的 mHandler 字段对象
            Field handlerField = tnField.getType().getDeclaredField("mHandler");
            handlerField.setAccessible(true);
            Handler handlerObject = (Handler) handlerField.get(tnObject);

            if (handlerObject == null) {
                // 这个对象是空的，说明 Toast 没有被正确创建出来，直接返回就好了
                return;
            }

            if (handlerObject instanceof SafeHandler) {
                // 如果这个对象已经被反射替换过了
                return;
            }

            // 偷梁换柱
            handlerField.set(tnObject, new SafeHandler(handlerObject));

        } catch (IllegalAccessException | NoSuchFieldException e) {
            // Android 9.0 上反射会出现报错
            // Accessing hidden field Landroid/widget/Toast;->mTN:Landroid/widget/Toast$TN;
            // java.lang.NoSuchFieldException: No field mTN in class Landroid/widget/Toast;
            e.printStackTrace();
        }
    }
}