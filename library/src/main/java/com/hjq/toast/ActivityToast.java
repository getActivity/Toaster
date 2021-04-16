package com.hjq.toast;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.hjq.toast.config.IToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/02
 *    desc   : 自定义 Toast（用于解决关闭通知栏权限之后不能弹吐司的问题和 Android 11 不能自定义吐司样式的问题）
 */
public final class ActivityToast implements IToast {

    /** Toast 实现类 */
    private final ToastImpl mToastImpl;

    /** Toast 布局 */
    private View mView;
    /** Toast 消息 View */
    private TextView mMessageView;
    /** Toast 显示重心 */
    private int mGravity;
    /** Toast 显示时长 */
    private int mDuration;
    /** 水平偏移 */
    private int mXOffset;
    /** 垂直偏移 */
    private int mYOffset;
    /** 水平间距 */
    private float mHorizontalMargin;
    /** 垂直间距 */
    private float mVerticalMargin;

    public ActivityToast(Activity activity) {
        mToastImpl = new ToastImpl(activity, this);
    }

    @Override
    public void show() {
        // 替换成 WindowManager 来显示
        mToastImpl.show();
    }

    @Override
    public void cancel() {
        // 取消 WindowManager 的显示
        mToastImpl.cancel();
    }

    @Override
    public void setText(int id) {
        if (mView == null) {
            return;
        }
        setText(mView.getResources().getString(id));
    }

    @Override
    public void setText(CharSequence text) {
        if (mMessageView == null) {
            return;
        }
        mMessageView.setText(text);
    }

    @Override
    public void setView(View view) {
        mView = view;
        if (mView == null) {
            mMessageView = null;
            return;
        }
        mMessageView = findMessageView(view);
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void setDuration(int duration) {
        mDuration = duration;
    }

    @Override
    public int getDuration() {
        return mDuration;
    }

    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mGravity = gravity;
        mXOffset = xOffset;
        mYOffset = yOffset;
    }

    @Override
    public int getGravity() {
        return mGravity;
    }

    @Override
    public int getXOffset() {
        return mXOffset;
    }

    @Override
    public int getYOffset() {
        return mYOffset;
    }

    @Override
    public void setMargin(float horizontalMargin, float verticalMargin) {
        mHorizontalMargin = horizontalMargin;
        mVerticalMargin = verticalMargin;
    }

    @Override
    public float getHorizontalMargin() {
        return mHorizontalMargin;
    }

    @Override
    public float getVerticalMargin() {
        return mVerticalMargin;
    }
}