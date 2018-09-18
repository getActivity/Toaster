package com.hjq.toast;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 *    author : HJQ
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : Toast工具类
 */
public final class ToastUtils {

    private static IToastStyle sDefaultStyle;

    private static Toast sToast;

    /**
     * 初始化ToastUtils，建议在Application中初始化
     *
     * @param context       应用的上下文
     */
    public static void init(Context context) {
        //检查默认样式是否为空，如果是就创建一个默认样式
        if (sDefaultStyle == null) {
            sDefaultStyle = new ToastBlackStyle();
        }

        //如果这个上下文不是全局的上下文，就自动换成全局的上下文
        if (context != context.getApplicationContext()) {
            context = context.getApplicationContext();
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(sDefaultStyle.getBackgroundColor());
        drawable.setCornerRadius(DimensUtils.dp2px(context, sDefaultStyle.getCornerRadius()));

        TextView textView = new TextView(context);
        textView.setTextColor(sDefaultStyle.getTextColor());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, DimensUtils.sp2px(context, sDefaultStyle.getTextSize()));
        textView.setPadding(DimensUtils.dp2px(context, sDefaultStyle.getPaddingLeft()), DimensUtils.dp2px(context, sDefaultStyle.getPaddingTop()),
                DimensUtils.dp2px(context, sDefaultStyle.getPaddingRight()), DimensUtils.dp2px(context, sDefaultStyle.getPaddingBottom()));
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //setBackground API版本兼容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(drawable);
        }else {
            textView.setBackgroundDrawable(drawable);
        }

        if (sDefaultStyle.getMaxLines() > 0) {
            textView.setMaxLines(sDefaultStyle.getMaxLines());
        }

        sToast = new XToast(context);
        sToast.setGravity(sDefaultStyle.getGravity(), sDefaultStyle.getXOffset(), sDefaultStyle.getYOffset());
        sToast.setView(textView);
    }

    /**
     * 显示一个对象的吐司
     *
     * @param object      对象
     */
    public static void show(Object object) {
        show(object != null ? object.toString() : "null");
    }

    /**
     * 显示一个吐司
     *
     * @param id      如果传入的是正确的string id就显示对应字符串
     *                如果不是则显示一个整数的string
     */
    public static void show(int id) {

        //吐司工具类还没有被初始化，必须要先调用init方法进行初始化
        if (sToast == null) {
            throw new IllegalStateException("ToastUtils has not been initialized");
        }

        try {
            //如果这是一个资源id
            show(sToast.getView().getContext().getResources().getText(id));
        } catch (Resources.NotFoundException ignored) {
            //如果这是一个int类型
            show(String.valueOf(id));
        }
    }

    /**
     * 显示一个吐司
     *
     * @param text      需要显示的文本
     */
    public static void show(CharSequence text) {

        //吐司工具类还没有被初始化，必须要先调用init方法进行初始化
        if (sToast == null) {
            throw new IllegalStateException("ToastUtils has not been initialized");
        }

        if (text == null || text.equals("")) return;

       //如果显示的文字超过了10个就显示长吐司，否则显示短吐司
        if (text.length() > 20) {
            sToast.setDuration(Toast.LENGTH_LONG);
        } else {
            sToast.setDuration(Toast.LENGTH_SHORT);
        }

        try {
            //判断是否在主线程中执行
            if (Looper.myLooper() == Looper.getMainLooper()) {
                sToast.setText(text);
                sToast.show();
            }else {
                //在子线程中显示处理
                Looper.prepare();
                sToast.setText(text);
                sToast.show();
                Looper.loop();
            }
        } catch (Exception ignored) {}
    }

    /**
     * 获取当前Toast对象
     */
    public static Toast getToast() {
        return sToast;
    }

    /**
     * 给当前Toast设置新的布局
     */
    public static void setView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("Views cannot be empty");
        }
        sToast.setView(view);
    }

    /**
     * 初始化Toast样式
     *
     * @param style         样式实现类
     */
    public static void initStyle(IToastStyle style) {
        ToastUtils.sDefaultStyle = style;
        //如果吐司已经创建，就重新初始化吐司
        if (sToast != null) {
            //取消原有吐司的显示
            sToast.cancel();
            //重新初始化吐司类
            init(sToast.getView().getContext().getApplicationContext());
        }
    }
}