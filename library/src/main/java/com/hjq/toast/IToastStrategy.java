package com.hjq.toast;

import android.app.Application;
import android.widget.Toast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2019/05/19
 *    desc   : Toast 处理策略
 */
public interface IToastStrategy {

    /** 短吐司显示的时长 */
    int SHORT_DURATION_TIMEOUT = 2000;
    /** 长吐司显示的时长 */
    int LONG_DURATION_TIMEOUT = 3500;

    /**
     * 创建 Toast
     */
    Toast create(Application application);

    /**
     * 绑定 Toast
     */
    void bind(Toast toast);

    /**
     * 显示 Toast
     */
    void show(CharSequence text);

    /**
     * 取消 Toast
     */
    void cancel();
}