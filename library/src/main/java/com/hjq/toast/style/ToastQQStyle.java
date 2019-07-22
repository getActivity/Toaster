package com.hjq.toast.style;

import android.content.Context;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : QQ 样式实现
 */
public class ToastQQStyle extends BaseToastStyle {

    public ToastQQStyle(Context context) {
        super(context);
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public int getCornerRadius() {
        return dp2px(4);
    }

    @Override
    public int getBackgroundColor() {
        return 0XFF333333;
    }

    @Override
    public int getTextColor() {
        return 0XFFE3E3E3;
    }

    @Override
    public float getTextSize() {
        return sp2px(12);
    }

    @Override
    public int getPaddingStart() {
        return dp2px(16);
    }

    @Override
    public int getPaddingTop() {
        return dp2px(14);
    }
}