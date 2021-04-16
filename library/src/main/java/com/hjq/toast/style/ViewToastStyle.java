package com.hjq.toast.style;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.hjq.toast.config.IToastStyle;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2021/03/09
 *    desc   : Toast View 包装样式实现
 */
public class ViewToastStyle implements IToastStyle<View> {

    private final int mLayoutId;
    private final IToastStyle<?> mStyle;

    public ViewToastStyle(int id, IToastStyle<?> style) {
        mLayoutId = id;
        mStyle = style;
    }

    @Override
    public View createView(Context context) {
        return LayoutInflater.from(context).inflate(mLayoutId, null);
    }

    @Override
    public int getGravity() {
        if (mStyle == null) {
            return Gravity.CENTER;
        }
        return mStyle.getGravity();
    }

    @Override
    public int getXOffset() {
        if (mStyle == null) {
            return 0;
        }
        return mStyle.getXOffset();
    }

    @Override
    public int getYOffset() {
        if (mStyle == null) {
            return 0;
        }
        return mStyle.getYOffset();
    }

    @Override
    public float getHorizontalMargin() {
        if (mStyle == null) {
            return 0;
        }
        return mStyle.getHorizontalMargin();
    }

    @Override
    public float getVerticalMargin() {
        if (mStyle == null) {
            return 0;
        }
        return mStyle.getVerticalMargin();
    }
}