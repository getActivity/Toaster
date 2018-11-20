package com.hjq.toast.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastBlackStyle;
import com.hjq.toast.style.ToastQQStyle;
import com.hjq.toast.style.ToastWhiteStyle;

public class ToastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast);
    }

    public void show1(final View v) {
        ToastUtils.show("我是一个普通的吐司");
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
        ToastUtils.show("动态切换吐司样式成功");
    }

    public void show4(View v) {
        ToastUtils.initStyle(new ToastBlackStyle());
        ToastUtils.show("动态切换吐司样式成功");
    }

    public void show5(View v) {
        ToastUtils.initStyle(new ToastQQStyle());
        ToastUtils.show("QQ样式那种还不简单，分分钟的事");
    }

    public void show6(View v) {
        ToastUtils.setView(this, R.layout.toast_custom_view);
        ToastUtils.show("我是自定义Toast");
    }

    public void show7(View v) {
        ToastUtils.show(ToastUtils.isNotificationEnabled(this));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 如果通知栏的权限被手动关闭了
        if (!ToastUtils.isNotificationEnabled(this) && "XToast".equals(ToastUtils.getToast().getClass().getSimpleName())) {
            // 因为吐司只有初始化的时候才会判断通知权限有没有开启，根据这个通知开关来显示原生的吐司还是兼容的吐司
            ToastUtils.init(getApplication());
            recreate();
            ToastUtils.show("检查到你手动关闭了通知权限，正在重新初始化框架，只有这样吐司才能正常显示出来");
        }
    }
}