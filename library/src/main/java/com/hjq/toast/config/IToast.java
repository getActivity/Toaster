package com.hjq.toast.config;

import android.view.View;
import android.widget.TextView;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2021/04/06
 *    desc   : Toast 接口
 */
@SuppressWarnings("unused")
public interface IToast {

    /**
     * 显示
     */
    void show();

    /**
     * 取消
     */
    void cancel();

    /**
     * 设置文本
     */
    void setText(int id);

    void setText(CharSequence text);

    /**
     * 设置布局
     */
    void setView(View view);

    /**
     * 获取布局
     */
    View getView();

    /**
     * 设置显示时长
     */
    void setDuration(int duration);

    /**
     * 获取显示时长
     */
    int getDuration();

    /**
     * 设置重心偏移
     */
    void setGravity(int gravity, int xOffset, int yOffset);

    /**
     * 获取显示重心
     */
    int getGravity();

    /**
     * 获取水平偏移
     */
    int getXOffset();

    /**
     * 获取垂直偏移
     */
    int getYOffset();

    /**
     * 设置屏幕间距
     */
    void setMargin(float horizontalMargin, float verticalMargin);

    /**
     * 设置水平间距
     */
    float getHorizontalMargin();

    /**
     * 设置垂直间距
     */
    float getVerticalMargin();

    /**
     * 智能获取用于显示消息的 TextView
     */
    default TextView findMessageView(View view) {
        if (view instanceof TextView) {
            if (view.getId() == View.NO_ID) {
                view.setId(android.R.id.message);
            } else if (view.getId() != android.R.id.message) {
                // 必须将 TextView 的 id 值设置成 android.R.id.message
                // 否则 Android 11 手机上在后台 toast.setText 的时候会出现报错
                // java.lang.RuntimeException: This Toast was not created with Toast.makeText()
                throw new IllegalArgumentException("You must set the ID value of TextView to android.R.id.message");
            }
            return (TextView) view;
        }

        View messageView = view.findViewById(android.R.id.message);
        if (messageView instanceof TextView) {
            return ((TextView) messageView);
        }

        // 如果设置的布局没有包含一个 TextView 则抛出异常，必须要包含一个 id 值为 message 的 TextView（xml 代码 android:id="@android:id/message"，java 代码 view.setId(android.R.id.message)）
        throw new IllegalArgumentException("You must include a TextView with an ID value of message (xml code: android:id=\"@android:id/message\", java code: view.setId(android.R.id.message))");
    }
}