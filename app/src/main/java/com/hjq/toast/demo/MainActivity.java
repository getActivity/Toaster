package com.hjq.toast.demo;

import android.content.Intent;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.WindowInsets;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.hjq.toast.ToastParams;
import com.hjq.toast.ToastStrategy;
import com.hjq.toast.Toaster;
import com.hjq.toast.style.BlackToastStyle;
import com.hjq.toast.style.CustomToastStyle;
import com.hjq.toast.style.WhiteToastStyle;
import com.hjq.window.EasyWindow;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/Toaster
 *    time   : 2018/09/01
 *    desc   : Toaster 使用案例
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

        // 适配 Android 15 EdgeToEdge 特性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            titleBar.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener()  {

                @NonNull
                @Override
                public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                    Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                    // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    v.setPadding(0, systemBars.top, 0, 0);
                    return insets;
                }
            });
        }
    }

    public void showToast(View v) {
        Toaster.show(R.string.demo_show_toast_result);
    }

    public void showShortToast(View v) {
        Toaster.showShort(R.string.demo_show_short_toast_result);
    }

    public void showLongToast(View v) {
        Toaster.showLong(R.string.demo_show_long_toast_result);
    }

    public void showCrossPageToast(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_show_cross_page_toast_result);
        params.priorityType = ToastParams.PRIORITY_TYPE_GLOBAL;
        Toaster.show(params);
    }

    public void delayShowToast(View v) {
        Toaster.delayedShow(R.string.demo_show_toast_with_two_second_delay_result, 2000);
    }

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void threadShowToast(View v) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Toaster.show(R.string.demo_show_toast_in_the_subthread_result);
            }
        }).start();
    }

    public void startActivityShowToast(View v) {
        Toaster.show(R.string.demo_show_toast_result);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void switchToastStyleToWhite(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_switch_to_white_style_result);
        params.style = new WhiteToastStyle();
        Toaster.show(params);
    }

    public void switchToastStyleToBlack(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_switch_to_black_style_result);
        params.style = new BlackToastStyle();
        Toaster.show(params);
    }

    public void switchToastStyleToInfo(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_switch_to_info_style_result);
        params.style = new CustomToastStyle(R.layout.toast_info);
        Toaster.show(params);
    }

    public void switchToastStyleToWarn(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_switch_to_warn_style_result);
        params.style = new CustomToastStyle(R.layout.toast_warn);
        Toaster.show(params);
    }

    public void switchToastStyleToSuccess(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_switch_to_success_style_result);
        params.style = new CustomToastStyle(R.layout.toast_success);
        Toaster.show(params);
    }

    public void switchToastStyleToError(View v) {
        ToastParams params = new ToastParams();
        params.text = getString(R.string.demo_switch_to_error_style_result);
        params.style = new CustomToastStyle(R.layout.toast_error);
        Toaster.show(params);
    }

    public void customGlobalToastStyle(View v) {
        Toaster.setView(R.layout.toast_custom_view);
        Toaster.setGravity(Gravity.CENTER);
        Toaster.show(R.string.demo_custom_toast_layout_result);
    }

    public void switchToastStrategy(View v) {
        Toaster.setStrategy(new ToastStrategy(ToastStrategy.SHOW_STRATEGY_TYPE_QUEUE));
        Toaster.show(R.string.demo_switch_to_toast_queuing_strategy_result);
        findViewById(R.id.tv_main_thrice_show).setVisibility(View.VISIBLE);
    }

    public void showThriceToast(View v) {
        for (int i = 0; i < 3; i++) {
            Toaster.show(String.format(getString(R.string.demo_show_three_toast_copywriting), i + 1));
        }
    }

    public void toBackgroundShowToast(View v) {
        Snackbar.make(getWindow().getDecorView(), getString(R.string.demo_show_toast_in_background_state_hint), Snackbar.LENGTH_SHORT).show();

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(getWindow().getDecorView(), getString(R.string.demo_show_toast_in_background_state_snack_bar), Snackbar.LENGTH_SHORT).show();
            }
        }, 2000);

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        }, 4000);

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (XXPermissions.isGrantedPermission(MainActivity.this, PermissionLists.getSystemAlertWindowPermission())) {
                        Toaster.show(R.string.demo_show_toast_in_background_state_result_1);
                    } else {
                        Toaster.show(R.string.demo_show_toast_in_background_state_result_2);
                    }
                } else {
                    Toaster.show(R.string.demo_show_toast_in_background_state_result_3);
                }
            }
        }, 5000);
    }

    public void combinationEasyWindowShow(View v) {
        new EasyWindow<>(this)
                // 将 Toaster 中的 View 转移给 EasyWindow 来显示
                .setContentView(Toaster.getStyle().createView(getApplication()))
                .setWindowDuration(1000)
                .setWindowAnim(android.R.style.Animation_Translucent)
                .setTextByTextView(android.R.id.message, R.string.demo_combining_window_framework_use_result)
                .setWindowLocation(Gravity.BOTTOM, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()))
                .show();
    }

    public void skipStacks(View v) {
        toastTraceAndSkipStacks(getString(R.string.demo_show_toast_with_trace_and_skip_stacks_result));
    }

    // 在封装的方法使用 Toaster 时，打印的栈信息跳过指定调用层数
    void toastTraceAndSkipStacks(String message) {
        ToastParams params = new ToastParams();
        params.text = message;
        params.stackSkips = 1; // 根据实际情况调整层数
        Toaster.show(params);
    }

}