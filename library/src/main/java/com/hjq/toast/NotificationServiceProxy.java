package com.hjq.toast;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2021/11/13
 *    desc   : 通知服务代理代理对象
 */
final class NotificationServiceProxy implements InvocationHandler {

    /** 被代理的对象 */
    private final Object mSource;

    public NotificationServiceProxy(Object source) {
        mSource = source;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "enqueueToast":
            case "enqueueToastEx":
            case "cancelToast":
                // 将包名修改成系统包名，这样就可以绕过系统的拦截
                // 部分华为机将 enqueueToast 修改成了 enqueueToastEx
                args[0] = "android";
                break;
            default:
                break;
        }
        // 使用动态代理
        return method.invoke(mSource, args);
    }
}