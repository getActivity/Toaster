package com.hjq.toast.style;

import android.content.Context;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : 默认黑色样式实现
 */
public class ToastBlackStyle extends BaseToastStyle {

    public ToastBlackStyle(Context context) {
        super(context);
    }

    @Override
    public int getCornerRadius() {
        return dp2px(8);
    }

    @Override
    public int getBackgroundColor() {
        return 0X88000000;
    }

    @Override
    public int getTextColor() {
        return 0XEEFFFFFF;
    }

    @Override
    public float getTextSize() {
        return sp2px(14);
    }

    @Override
    public int getPaddingStart() {
        return dp2px(24);
    }

    @Override
    public int getPaddingTop() {
        return dp2px(16);
    }
}