package com.hjq.toast.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastBlackStyle;
import com.hjq.toast.style.ToastQQStyle;
import com.hjq.toast.style.ToastWhiteStyle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void show1(final View v) {
        ToastUtils.show("我是吐司");
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
        ToastUtils.show(this);
    }
}