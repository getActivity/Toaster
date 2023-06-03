package com.hjq.toast;

import android.util.Log;

import com.hjq.toast.config.IToastInterceptor;

import java.lang.reflect.Modifier;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2020/11/04
 *    desc   : 自定义 Toast 拦截器（用于追踪 Toast 调用的位置）
 */
public class ToastLogInterceptor implements IToastInterceptor {

    @Override
    public boolean intercept(ToastParams params) {
        printToast(params.text);
        return false;
    }

    protected void printToast(CharSequence text) {
        if (!isLogEnable()) {
            return;
        }

        // 获取调用的堆栈信息
        StackTraceElement[] stackTraces = new Throwable().getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            // 获取代码行数
            int lineNumber = stackTrace.getLineNumber();
            if (lineNumber <= 0) {
                continue;
            }

            // 获取类的全路径
            String className = stackTrace.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                if (!filterClass(clazz)) {
                    printLog("(" + stackTrace.getFileName() + ":" + lineNumber + ") " + text.toString());
                    // 跳出循环
                    break;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean isLogEnable() {
        return Toaster.isDebugMode();
    }

    protected void printLog(String msg) {
        // 这里解释一下，为什么不用 Log.d，而用 Log.i，因为 Log.d 在魅族 16th 手机上面无法输出日志
        Log.i("Toaster", msg);
    }

    protected boolean filterClass(Class<?> clazz) {
        // 排查以下几种情况：
        // 1. 排除自身及其子类
        // 2. 排除 Toaster 类
        // 3. 排除接口类
        // 4. 排除抽象类
        return IToastInterceptor.class.isAssignableFrom(clazz) ||
                Toaster.class.equals(clazz) ||
                clazz.isInterface() ||
                Modifier.isAbstract(clazz.getModifiers());
    }
}