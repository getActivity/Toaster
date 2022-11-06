package com.hjq.toast.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastParams;
import com.hjq.toast.ToastStrategy;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.BlackToastStyle;
import com.hjq.toast.style.CustomViewToastStyle;
import com.hjq.toast.style.WhiteToastStyle;
import com.hjq.xtoast.XToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : ToastUtils 使用案例
 */
public final class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TitleBar titleBar = findViewById(R.id.tb_main_bar);
        titleBar.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onTitleClick(TitleBar titleBar) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(titleBar.getTitle().toString()));
                startActivity(intent);
            }
        });
    }

    public void showToast(View v) {
        ToastUtils.show("我是普通的 Toast");
    }

    public void showThriceToast(View v) {
        for (int i = 0; i < 3; i++) {
            ToastUtils.show("我是第 " + (i + 1) + " 个 Toast");
        }
    }

    public void delayShowToast(View v) {
        ToastUtils.delayedShow("我是延迟 2 秒显示的 Toast", 2000);
    }

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void threadShowToast(View v) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                ToastUtils.show("我是子线程中弹出的吐司");
            }
        }).start();
    }

    public void switchToastStyleToWhite(View v) {
        ToastUtils.setStyle(new WhiteToastStyle());
        ToastUtils.show("动态切换白色吐司样式成功");
    }

    public void switchToastStyleToBlack(View v) {
        ToastUtils.setStyle(new BlackToastStyle());
        ToastUtils.show("动态切换黑色吐司样式成功");
    }

    public void customLocalToastStyle(View v) {
        ToastParams params = new ToastParams();
        params.text = "我是自定义布局的 Toast（局部生效）";
        params.style = new CustomViewToastStyle(R.layout.toast_custom_view);
        ToastUtils.show(params);
    }

    public void customGlobalToastStyle(View v) {
        ToastUtils.setView(R.layout.toast_custom_view);
        ToastUtils.setGravity(Gravity.CENTER);
        ToastUtils.show("我是自定义布局的 Toast（全局生效）");
    }

    public void switchToastStrategy(View v) {
        ToastUtils.setStrategy(new ToastStrategy(ToastStrategy.SHOW_STRATEGY_TYPE_QUEUE));
        ToastUtils.show("切换到排队显示策略成功，请点击下方文本来体验效果");
        findViewById(R.id.tv_main_thrice_show).setVisibility(View.VISIBLE);
    }

    public void toBackgroundShowToast(View v) {
        Snackbar.make(getWindow().getDecorView(), "温馨提示：安卓 10 在后台显示 Toast 需要有通知栏权限或者悬浮窗权限的情况下才可以显示", Snackbar.LENGTH_SHORT).show();

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
                    if (XXPermissions.isGranted(MainActivity.this, Permission.SYSTEM_ALERT_WINDOW)) {
                        ToastUtils.show("我是在后台显示的 Toast（有悬浮窗权限真的可以为所欲为）");
                    } else {
                        ToastUtils.show("我是在后台显示的 Toast（安卓 11 及以上在后台显示只能使用系统样式）");
                    }
                } else {
                    ToastUtils.show("我是在后台显示的 Toast");
                }
            }
        }, 3000);
    }

    public void combinationXToastShow(View v) {
        new XToast<>(this)
                .setDuration(1000)
                // 将 ToastUtils 中的 View 转移给 XToast 来显示
                .setContentView(ToastUtils.getStyle().createView(getApplication()))
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setText(android.R.id.message, "就问你溜不溜")
                .setGravity(Gravity.BOTTOM)
                .setYOffset((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()))
                .show();
    }
}