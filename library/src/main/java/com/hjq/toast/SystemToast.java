package com.hjq.toast;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.hjq.toast.config.IToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2018/11/03
 *    desc   : 系统 Toast
 */
@SuppressWarnings("deprecation")
public class SystemToast extends Toast implements IToast {

    @NonNull
    private final Application mApplication;

    /** 吐司消息 View */
    @Nullable
    private TextView mMessageView;

    public SystemToast(@NonNull Application application) {
        super(application);
        mApplication = application;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mApplication;
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        if (view == null) {
            mMessageView = null;
            return;
        }
        mMessageView = findMessageView(view);
    }

    @Override
    public void setText(@NonNull CharSequence text) {
        if (mMessageView == null) {
            try {
                super.setText(text);
            } catch (Exception e) {
                // Github issue 地址：https://github.com/getActivity/Toaster/issues/160
                // java.lang.RuntimeException: This Toast was not created with Toast.makeText()
                e.printStackTrace();
            }
            return;
        }
        mMessageView.setText(text);
    }
}