package com.hjq.toast.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastAliPayStyle;
import com.hjq.toast.style.ToastBlackStyle;
import com.hjq.toast.style.ToastQQStyle;
import com.hjq.toast.style.ToastWhiteStyle;
import com.hjq.xtoast.XToast;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ToastUtils
 *    time   : 2018/09/01
 *    desc   : ToastUtils 使用案例
 */
public class ToastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);
    }

    public void show1(View v) {
        for (int i = 0; i < 3; i++) {
            ToastUtils.show("我是第" + (i + 1) + "个吐司");
        }
    }

    public void show2(View v) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                ToastUtils.show("我是子线程中弹出的吐司");
            }
        }).start();
    }

    public void show3(View v) {
        ToastUtils.initStyle(new ToastWhiteStyle());
        ToastUtils.show("动态切换白色吐司样式成功");
    }

    public void show4(View v) {
        ToastUtils.initStyle(new ToastBlackStyle());
        ToastUtils.show("动态切换黑色吐司样式成功");
    }

    public void show5(View v) {
        ToastUtils.initStyle(new ToastQQStyle());
        ToastUtils.show("QQ那种还不简单，分分钟的事");
    }

    public void show6(View v) {
        ToastUtils.initStyle(new ToastAliPayStyle());
        ToastUtils.show("支付宝那种还不简单，分分钟的事");
    }

    public void show7(View v) {
        // ToastUtils.setView(View.inflate(getApplication(), R.layout.toast_custom_view, null));
        ToastUtils.setView(R.layout.toast_custom_view);
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.show("我是自定义Toast");
    }

    public void show8(View v) {
        new XToast(ToastActivity.this)
                .setDuration(1000)
                .setView(ToastUtils.getToast().getView())
                .setAnimStyle(android.R.style.Animation_Translucent)
                .setText(android.R.id.message, "就问你溜不溜")
                .show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 请注意这段代码强烈建议不要放到实际开发中，因为用户屏蔽通知栏和开启应用状态下的情况极少，可以忽略不计

        // 如果通知栏的权限被手动关闭了
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled() &&
                !"SupportToast".equals(ToastUtils.getToast().getClass().getSimpleName())) {
            try {
                // 因为吐司只有初始化的时候才会判断通知权限有没有开启，根据这个通知开关来显示原生的吐司还是兼容的吐司
                ToastUtils.init(getApplication());
                recreate();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ToastUtils.show("检查到你手动关闭了通知权限，正在重新初始化ToastUtils框架");
                        } catch (Exception ignored) {}
                    }
                }, 2000);
            }catch (Exception ignored) {}
        }
    }
}