package com.hjq.toast;

import android.annotation.SuppressLint;
import android.app.Application;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2021/11/24
 *    desc   : 处理 Toast 关闭通知栏权限之后无法弹出的问题
 */
public class NotificationToast extends SystemToast {

    /** 是否已经 Hook 了一次通知服务 */
    private static boolean sHookService;

    public NotificationToast(Application application) {
        super(application);
    }

    @Override
    public void show() {
        hookNotificationService();
        super.show();
    }

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    @SuppressWarnings({"JavaReflectionMemberAccess", "SoonBlockedPrivateApi"})
    private static void hookNotificationService() {
        if (sHookService) {
            return;
        }
        sHookService = true;
        try {
            // 获取到 Toast 中的 getService 静态方法
            Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
            getServiceMethod.setAccessible(true);
            // 执行方法，会返回一个 INotificationManager$Stub$Proxy 类型的对象
            final Object notificationManagerSourceObject = getServiceMethod.invoke(null);
            if (notificationManagerSourceObject == null) {
                return;
            }
            // 如果这个对象已经被动态代理过了，并且已经 Hook 过了，则不需要重复 Hook
            if (Proxy.isProxyClass(notificationManagerSourceObject.getClass()) &&
                    Proxy.getInvocationHandler(notificationManagerSourceObject) instanceof NotificationServiceProxy) {
                return;
            }
            Object notificationManagerProxyObject = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[]{Class.forName("android.app.INotificationManager")},
                    new NotificationServiceProxy(notificationManagerSourceObject));
            // 将原来的 INotificationManager$Stub$Proxy 替换掉
            Field serviceField = Toast.class.getDeclaredField("sService");
            serviceField.setAccessible(true);
            serviceField.set(null, notificationManagerProxyObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}