package com.hjq.toast.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.BlackToastStyle;
import com.hjq.toast.style.WhiteToastStyle;
import com.hjq.xtoast.XToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : ToastUtils 使用案例
 */
public final class ToastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);
    }

    public void show1(View v) {
        ToastUtils.show("我是普通的 Toast");
    }

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void show2(View v) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                ToastUtils.show("我是子线程中弹出的吐司");
            }
        }).start();
    }

    public void show3(View v) {
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.show("动态切换白色吐司样式成功");
    }

    public void show4(View v) {
        ToastUtils.setStyle(new BlackToastStyle());
        ToastUtils.show("动态切换黑色吐司样式成功");
    }

    public void show5(View v) {
        ToastUtils.setView(R.layout.toast_custom_view);
        ToastUtils.setGravity(Gravity.CENTER);
        ToastUtils.show("自定义 Toast 布局");
    }

    public void show6(View v) {
        if (XXPermissions.isGranted(this, Permission.NOTIFICATION_SERVICE)) {

            Snackbar.make(getWindow().getDecorView(), "正在准备跳转到手机桌面，请注意有极少数机型无法在后台显示 Toast", Snackbar.LENGTH_SHORT).show();

            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }, 2000);

            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ToastUtils.show("我是在后台显示的 Toast（在 Android 11 上只能跟随系统 Toast 样式）");
                    } else {
                        ToastUtils.show("我是在后台显示的 Toast");
                    }
                }
            }, 3000);

        } else {

            ToastUtils.show("在后台显示 Toast 需要先获取通知栏权限");
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    XXPermissions.startPermissionActivity(ToastActivity.this, Permission.NOTIFICATION_SERVICE);
                }
            }, 2000);
        }
    }

    public void show7(View v) {
        new XToast<>(ToastActivity.this)
                .setDuration(1000)
                .setView(ToastUtils.getStyle().createView(getApplication()))
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setText(android.R.id.message, "就问你溜不溜")
                .setGravity(Gravity.BOTTOM)
                .setYOffset((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()))
                .show();
    }
}