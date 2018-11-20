package com.hjq.toast;

import android.app.Application;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 *    author : HJQ
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/11/03
 *    desc   : Toast 优化类
 */
class XToast extends Toast {

    // 吐司消息 View
    private TextView mMessageView;

    XToast(Application application) {
        super(application);
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        if (view instanceof TextView) {
            mMessageView = (TextView) view; return;
        }else if (view.findViewById(android.R.id.message) instanceof TextView) {
            mMessageView = ((TextView) view.findViewById(android.R.id.message)); return;
        } else if (view instanceof ViewGroup) {
            if ((mMessageView = findTextView((ViewGroup) view)) != null) return;
        }
        // 如果设置的布局没有包含一个 TextView 则抛出异常，必须要包含一个 TextView 作为 Message View
        throw new IllegalArgumentException("The layout must contain a TextView");
    }

    @Override
    public void setText(CharSequence s) {
        mMessageView.setText(s);
    }

    /**
     * 递归获取ViewGroup中的TextView对象
     */
    private static TextView findTextView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            if ((view instanceof TextView)) {
                return (TextView) view;
            } else if (view instanceof ViewGroup) {
                TextView textView = findTextView((ViewGroup) view);
                if (textView != null) return textView;
            }
        }
        return null;
    }
}