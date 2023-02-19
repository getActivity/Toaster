#### Catalog

* [How to customize toast display animation](#how-to-customize-toast-display-animation)

* [How to customize toast display duration](#how-to-customize-toast-display-duration)

* [How to customize toast layout style](#how-to-customize-toast-layout-style)

* [How to switch to toast queue display strategy](#how-to-switch-to-toast-queue-display-strategy)

* [What should I do if the framework cannot meet the scene I am currently using](#what-should-i-do-if-the-framework-cannot-meet-the-scene-i-am-currently-using)

* [Why the framework prefers to use window manager to implement toast](#why-the-framework-prefers-to-use-window-manager-to-implement-toast)

#### How to customize toast display animation

* When toast is initialized, just modify the toast strategy

```java
Toaster.init(this, new ToastStrategy() {

    @Override
    public IToast createToast(IToastStyle<?> style) {
        if (toast instanceof CustomToast) {
            CustomToast customToast = ((CustomToast) toast);
            // Set the toast animation effect
            customToast.setAnimationsId(R.anim.xxx);
        }
        return toast;
    }
});
```

* The disadvantage of this method is that it will only take effect when the application is in the foreground. This is because the toast in the foreground is implemented with a framework, which is essentially a WindowManager. The advantage is that it is very flexible and is not limited by the system toast mechanism. The disadvantage is that it cannot It is displayed in the background; while the toast in the background is implemented by the system, the advantage is that it can be displayed in the background, the disadvantage is that it is very limited and cannot be customized too deeply; and the framework uses two The advantages and disadvantages of the two methods are complementary.

#### How to customize toast display duration

* When toast is initialized, just modify the toast strategy

```java
Toaster.init(this, new ToastStrategy() {

    @Override
    public IToast createToast(IToastStyle<?> style) {
        IToast toast = super.createToast(style);
        if (toast instanceof CustomToast) {
            CustomToast customToast = ((CustomToast) toast);
            // Set the display duration of the short toast (default is 2000 milliseconds)
            customToast.setShortDuration(1000);
            // Set the display duration of the long Toast (default is 3500 milliseconds)
            customToast.setLongDuration(5000);
        }
        return toast;
    }
});
```

* The disadvantage of this method is that it will only take effect when the application is in the foreground. This is because the toast in the foreground is implemented with a framework, which is essentially a WindowManager. The advantage is that it is very flexible and is not limited by the system toast mechanism. The disadvantage is that it cannot It is displayed in the background; while the toast in the background is implemented by the system, the advantage is that it can be displayed in the background, the disadvantage is that it is very limited and cannot be customized too deeply; and the framework uses two The advantages and disadvantages of the two methods are complementary.

#### How to customize toast layout style

* If you want to set the global toast style, you can call it like this (choose any one)

```java
// Modify toast layout
Toaster.setView(int id);
```

```java
// Modified toast layout, toast shows center of gravity, toast shows position offset
Toaster.setStyle(IToastStyle<?> style);
```

* If you want to set a separate Toast display style for one occasion, you can do all of these (select either)

```java
// Modify toast layout
ToastParams params = new ToastParams();
params.text = "I am toast of custom layout (partial effect)";
params.style = new CustomViewToastStyle(R.layout.toast_custom_view);
Toaster.show(params);
```

```java
// Modify the toast layout, toast display center of gravity, and toast display position offset
ToastParams params = new ToastParams();
params.text = "I am toast of custom layout (partial effect)";
params.style = new CustomViewToastStyle(R.layout.toast_custom_view, Gravity.CENTER, 10, 20);
Toaster.show(params);
```

* At this point, you may have a doubt, why setting a new toast style can only pass in the layout id instead of the View object? Because every time the framework displays toast, it will create a new toast object and View object. If the View object is passed in, it will not be able to create it every time it is displayed. As for why the framework does not reuse this View object, it is because if After reusing this View object, the following exceptions may be triggered:

```text
java.lang.IllegalStateException: View android.widget.TextView{7ffea98 V.ED..... ......ID 0,0-396,153 #102000b android:id/message} 
has already been added to the window manager.
    at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:371)
    at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:131)
    at android.widget.Toast$TN.handleShow(Toast.java:501)
    at android.widget.Toast$TN$1.handleMessage(Toast.java:403)
    at android.os.Handler.dispatchMessage(Handler.java:112)
    at android.os.Looper.loop(Looper.java:216)
    at android.app.ActivityThread.main(ActivityThread.java:7625)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:524)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:987)
```

* This is because WindowManager succeeded when addingView, but failed when removingView, which caused the View object of the previous toast to be unable to be reused when the next toast is displayed. Although this situation is relatively rare, there are still people who have reported this to me. Problem, in order to solve this problem, I decided not to reuse the View object. For specific adjustments to this piece, you can check the release record: [Toaster/releases/tag/9.0](https://github.com/getActivity/Toaster/releases/tag/9.0)

#### How to switch to toast queue display strategy

* You only need to modify the initialization method of the toast framework and manually pass in the toast strategy class. Here, you can use the ToastStrategy class that has been encapsulated by the framework.

```java
// Initialize the toast framework
// Toaster.init(this);
Toaster.init(this, new ToastStrategy(ToastStrategy.SHOW_STRATEGY_TYPE_QUEUE));
```

* Note that the constructor needs to pass in `ToastStrategy.SHOW_STRATEGY_TYPE_QUEUE`. For an introduction to this field, see the code comments below

```java
public class ToastStrategy {

    /**
     * Instant display mode (default)
     *
     * In the case of multiple toast display requests, before displaying the next toast
     * The previous toast will be canceled immediately to ensure that the currently displayed toast message is up to date
     */
    public static final int SHOW_STRATEGY_TYPE_IMMEDIATELY = 0;

    /**
     * No message loss mode
     *
     * In the case of multiple toast display requests, wait for the previous toast to be displayed for 1 second or 1.5 seconds
     * Then display the next toast, not according to the display duration of the toast, because the waiting time will be very long
     * This can not only ensure that the user can see every toast message, but also ensure that the user will not wait too long
     */
    public static final int SHOW_STRATEGY_TYPE_QUEUE = 1;
}
```

#### What should I do if the framework cannot meet the scene I am currently using

* The Toaster framework is intended to solve some toast requirements. If Toaster cannot meet your needs, you can consider using the [XToast](https://github.com/getActivity/XToast) floating window framework to achieve it.

#### Why the framework prefers to use window manager to implement toast

* There are too many pits in the system toast, the main problems are as follows:

    * System toast will cause some memory leaks

    * System toast cannot realize custom display animation and display duration control

    * Android 7.1 version will block the main thread and cause BadTokenException

    * Closing the permission of the notification bar below Android 10.0 will cause the problem that the system toast cannot be displayed

    * Android 11 and above, cannot customize the toast style (layout, position center of gravity, position offset)

* Therefore, the framework prefers to use WindowManager instead of implementing toast display. The specific advantages and disadvantages are as follows:

    * advantage

        * There will be no memory leaks, and there will not be so many strange problems

        * High degree of customization, support custom animation and custom display duration

        * Break through Google's restrictions on toast in the new version of Android

    * shortcoming

        *  WindowManager cannot pop up <br> in the background without floating window permission (frame solution: if it is displayed in the background, use the system's toast to display)

       *  The WindowManager will be bound to the Activity and will disappear with the Activity being destroyed <br> (framework solution: the display is delayed by 200ms, thus waiting for the latest Activity to be created before calling the display, so WindowManager is bound to the latest Activity and does not have the problem of disappearing with the old Activity when it finishes)

* Of course, it is not to say that using the system toast is not good. It must be good to use WindowManger. It depends on the specific usage scenario. I think the best way is: use WindowManager to display the application in the foreground, and use the system in the background. the best solution is to use WindowManager in the foreground state and system Toast in the background state.
