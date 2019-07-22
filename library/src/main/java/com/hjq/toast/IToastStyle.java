package com.hjq.toast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : 默认样式接口
 */
public interface IToastStyle {

    /**
     * 吐司的重心
     */
    int getGravity();

    /**
     * X轴偏移
     */
    int getXOffset();

    /**
     * Y轴偏移
     */
    int getYOffset();

    /**
     * 吐司 Z 轴坐标
     */
    int getZ();

    /**
     * 圆角大小
     */
    int getCornerRadius();

    /**
     * 背景颜色
     */
    int getBackgroundColor();

    /**
     * 文本颜色
     */
    int getTextColor();

    /**
     * 文本大小
     */
    float getTextSize();

    /**
     * 最大行数
     */
    int getMaxLines();

    /**
     * 开始内边距
     */
    int getPaddingStart();

    /**
     * 顶部内边距
     */
    int getPaddingTop();

    /**
     * 结束内边距
     */
    int getPaddingEnd();

    /**
     * 底部内边距
     */
    int getPaddingBottom();
}