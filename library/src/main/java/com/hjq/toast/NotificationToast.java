package com.hjq.toast;

import android.annotation.SuppressLint;
import android.app.Application;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

    static {
        hookNotificationService();
    }

    public NotificationToast(@NonNull Application application) {
        super(application);
    }

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    @SuppressWarnings({"JavaReflectionMemberAccess", "SoonBlockedPrivateApi"})
    private static void hookNotificationService() {
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

            ClassLoader classLoader = notificationManagerSourceObject.getClass().getClassLoader();
            Class<?> clazz = Class.forName("android.app.INotificationManager", false, classLoader);
            Object notificationManagerProxyObject = Proxy.newProxyInstance(classLoader, new Class[]{clazz},
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