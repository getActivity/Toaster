package com.hjq.toast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : 默认样式接口
 */
public interface IToastStyle {

    int getGravity(); // 吐司的重心
    int getXOffset(); // X轴偏移
    int getYOffset(); // Y轴偏移
    int getZ(); // 吐司Z轴坐标

    int getCornerRadius(); // 圆角大小
    int getBackgroundColor(); // 背景颜色

    int getTextColor(); // 文本颜色
    float getTextSize(); // 文本大小
    int getMaxLines(); // 最大行数

    int getPaddingLeft(); // 左边内边距
    int getPaddingTop(); // 顶部内边距
    int getPaddingRight(); // 右边内边距
    int getPaddingBottom(); // 底部内边距
}