package com.hjq.toast.style;

import android.view.Gravity;

import com.hjq.toast.IToastStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : QQ样式实现
 */
public class ToastQQStyle implements IToastStyle {

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public int getXOffset() {
        return 0;
    }

    @Override
    public int getYOffset() {
        return 0;
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public int getCornerRadius() {
        return 4;
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
        return 12;
    }

    @Override
    public int getMaxLines() {
        return 3;
    }

    @Override
    public int getPaddingLeft() {
        return 16;
    }

    @Override
    public int getPaddingTop() {
        return 14;
    }

    @Override
    public int getPaddingRight() {
        return getPaddingLeft();
    }

    @Override
    public int getPaddingBottom() {
        return getPaddingTop();
    }
}