package com.hjq.toast;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Looper;
import android.util.TypedValue;
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
        textView.setBackgroundDrawable(drawable);
        if (sDefaultStyle.getMaxLines() > 0) {
            textView.setMaxLines(sDefaultStyle.getMaxLines());
        }

        sToast = new Toast(context);
        sToast.setGravity(sDefaultStyle.getGravity(), sDefaultStyle.getXOffset(), sDefaultStyle.getYOffset());
        sToast.setView(textView);
    }

    /**
     * 显示一个对象的吐司
     *
     * @param object      对象
     */
    public static void show(Object object) {
        if (object != null) {
            show(object.toString());
        }else {
            show("null");
        }
    }

    /**
     * 显示一个吐司
     *
     * @param text      需要显示的文本
     */
    public static void show(CharSequence text) {

        if (sToast == null || text == null || text.equals("")) return;

        //如果显示的文字超过了10个就显示长吐司，否则显示短吐司
        if (text.length() > 20) {
            sToast.setDuration(Toast.LENGTH_LONG);
        } else {
            sToast.setDuration(Toast.LENGTH_SHORT);
        }

        //子线程中异常情况处理
        try {
            ((TextView) sToast.getView()).setText(text);
            sToast.show();
        } catch (RuntimeException e) {
            try {
                Looper.prepare();
                ((TextView) sToast.getView()).setText(text);
                sToast.show();
                Looper.loop();
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    /**
     * 获取当前Toast对象
     */
    public static Toast getToast() {
        return sToast;
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
            init(sToast.getView().getContext().getApplicationContext());
        }
    }
}