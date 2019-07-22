package com.hjq.toast.style;

import android.content.Context;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : 默认白色样式实现
 */
public class ToastWhiteStyle extends ToastBlackStyle {

    public ToastWhiteStyle(Context context) {
        super(context);
    }

    @Override
    public int getBackgroundColor() {
        return 0XFFEAEAEA;
    }

    @Override
    public int getTextColor() {
        return 0XBB000000;
    }
}