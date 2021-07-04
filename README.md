# 吐司框架

* 码云地址：[Gitee](https://gitee.com/getActivity/ToastUtils)

* 博客地址：[只需体验三分钟，你就会跟我一样，爱上这款 Toast](https://www.jianshu.com/p/9b174ee2c571)

* 已投入公司项目多时，没有任何毛病，可胜任任何需求，[点击此处下载Demo](ToastUtils.apk)

![](ToastUtils.jpg)

#### 集成步骤

* 在项目根目录下的 `build.gradle` 文件中加入

```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

* 在项目 app 模块下的 `build.gradle` 文件中加入

```groovy
android {
    // 支持 JDK 1.8
    compileOptions {
        targetCompatibility JavaVersion.VERSION_1_8
        sourceCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    // 吐司框架：https://github.com/getActivity/ToastUtils
    implementation 'com.github.getActivity:ToastUtils:9.5'
}
```

#### 初始化框架

```java
public class XxxApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 Toast 框架
        ToastUtils.init(this);
    }
}
```

#### 显示 Toast

```java
ToastUtils.show("我是吐司");
```

#### 取消 Toast

```java
ToastUtils.cancel();
```

#### 其他 API

```java
// 设置 Toast 布局及样式
ToastUtils.setStyle(IToastStyle<?> style);

// 设置 Toast 重心和偏移
ToastUtils.setGravity(int gravity, int xOffset, int yOffset);

// 设置 Toast 拦截器
ToastUtils.setInterceptor(IToastInterceptor interceptor);

// 设置 Toast 策略
ToastUtils.setStrategy(IToastStrategy strategy);

// 设置 Toast 布局
ToastUtils.setView(int id);
```

#### 框架亮点

* 一马当先：首款适配 Android 11 的吐司框架，使用者无需关心适配过程

* 无需权限：[不管有没有授予通知栏权限都不影响吐司的弹出](https://www.jianshu.com/p/1d64a5ccbc7c)

* 兼容性强：[处理原生 Toast 在 Android 7.1 产生崩溃的历史遗留问题](https://www.jianshu.com/p/437f473017d6)

* 功能强大：不分主次线程都可以弹出Toast，自动区分资源 id 和 int 类型

* 使用简单：只需传入文本，会自动根据文本长度决定吐司显示的时长

* 性能最佳：使用懒加载模式，只在显示时创建 Toast，不占用 Application 启动时间

* 体验最佳：显示下一个 Toast 会取消上一个 Toast 的显示，真正做到即显即示

* 全局统一：可以在 Application 中初始化 Toast 样式，达到一劳永逸的效果

#### 关于通知栏权限

* 本框架已经完美解决这个问题，即使没有通知栏权限的情况下也能在前台显示 Toast

* 具体解决方案参见：[Toast通知栏权限填坑指南](https://www.jianshu.com/p/1d64a5ccbc7c)

![](issue_taobao.gif)

![](issue_utils.gif)

#### 如何替换项目中已有的原生 Toast

* 在项目中右击弹出菜单，Replace in path，勾选 Regex 选项，点击替换

```java
Toast\.makeText\([^,]+,\s*(.+{1}),\s*[^,]+\)\.show\(\)
```

---

```java
ToastUtils.show($1)
```

* 对导包进行替换

```java
import android.widget.Toast
```

---

```java
import com.hjq.toast.ToastUtils
```

*  再全局搜索，手动更换一些没有替换成功的

```java
Toast.makeText
```

---

```java
new Toast
```

#### 温馨提示：框架意在解决一些常规的 Toast 需求，如果是有一些特殊的定制化需求请配搭 [XToast](https://github.com/getActivity/XToast) 悬浮窗框架使用

#### 作者的其他开源项目

* 安卓技术中台：[AndroidProject](https://github.com/getActivity/AndroidProject)

* 网络框架：[EasyHttp](https://github.com/getActivity/EasyHttp)

* 权限框架：[XXPermissions](https://github.com/getActivity/XXPermissions)

* 标题栏框架：[TitleBar](https://github.com/getActivity/TitleBar)

* 国际化框架：[MultiLanguages](https://github.com/getActivity/MultiLanguages)

* 悬浮窗框架：[XToast](https://github.com/getActivity/XToast)

* Gson 解析容错：[GsonFactory](https://github.com/getActivity/GsonFactory)

* 日志查看框架：[Logcat](https://github.com/getActivity/Logcat)

#### 微信公众号：Android轮子哥

![](https://raw.githubusercontent.com/getActivity/Donate/master/picture/official_ccount.png)

#### Android 技术分享 QQ 群：78797078

#### 如果您觉得我的开源库帮你节省了大量的开发时间，请扫描下方的二维码随意打赏，要是能打赏个 10.24 :monkey_face:就太:thumbsup:了。您的支持将鼓励我继续创作:octocat:

![](https://raw.githubusercontent.com/getActivity/Donate/master/picture/pay_ali.png) ![](https://raw.githubusercontent.com/getActivity/Donate/master/picture/pay_wechat.png)

#### [点击查看捐赠列表](https://github.com/getActivity/Donate)

## License

```text
Copyright 2018 Huang JinQun

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
