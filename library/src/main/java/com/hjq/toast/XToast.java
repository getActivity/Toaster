package com.hjq.toast;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 *    author : HJQ
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/17
 *    desc   : 加强版Toast
 */
final class XToast extends Toast {

    private TextView mTextView;

    XToast(Context context) {
        super(context);
    }

    @Override
    public final void setView(View view) {
        super.setView(view);
        if (view instanceof TextView) {
            mTextView = (TextView) view;
            return;
        }else if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                if (((ViewGroup) view).getChildAt(i) instanceof TextView) {
                    mTextView = (TextView) ((ViewGroup) view).getChildAt(i);
                    return;
                }
            }
        }
        //如果设置的布局没有包含一个TextView则抛出异常，必须要包含一个TextView作为Message对象
        throw new IllegalArgumentException("The layout must contain a TextView");
    }

    @Override
    public final void setText(CharSequence s) {
        if (mTextView != null) {
            mTextView.setText(s);
        }else {
            super.setText(s);
        }
    }
}