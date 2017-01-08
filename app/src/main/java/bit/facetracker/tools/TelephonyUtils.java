package bit.facetracker.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class TelephonyUtils {

    private TelephonyUtils() {

    }

    public static int dp2px(Context context, float dpValue) {
        if (null == context || null == context.getResources()
                || null == context.getResources().getDisplayMetrics()) {
            return 0;
        }
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static int px2dp(Context context, float pxValue) {
        if (context == null) {
            return 0;
        }
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static Intent getSmsIntent(Context context, String content) {
        Intent smsIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            smsIntent = getNewSmsIntent(context, content);
        } else {
            smsIntent = new Intent(Intent.ACTION_VIEW);
            // smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.setData(Uri.parse("sms:"));
            smsIntent.putExtra("sms_body", content);
        }
        return smsIntent;
    }

    @SuppressLint("NewApi")
    private static Intent getNewSmsIntent(Context context, String content) {
        Intent smsIntent;
        String defaultSmsPackageName = Telephony.Sms
                .getDefaultSmsPackage(context);
        smsIntent = new Intent(Intent.ACTION_SEND);
        smsIntent.setType("text/plain");
        smsIntent.putExtra(Intent.EXTRA_TEXT, content);
        if (defaultSmsPackageName != null) {
            smsIntent.setPackage(defaultSmsPackageName);
        }
        return smsIntent;
    }

    public static Intent getPhoneCallIntent(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        return intent;
    }

    /**
     * getDeviceInfo:获取友盟集成测试需要的设备信息. <br/>
     *
     * @param context
     * @return
     * @author Zhong Likui
     */
    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * getDisplayMetrics:返回DisplayMetrics对象，以方便得到屏幕相关信息. <br/>
     *
     * @param context
     * @return
     * @author zhaolei
     */
    public static DisplayMetrics getDeviceDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        try {
            WindowManager manager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            if (display != null) {
                display.getMetrics(dm);
            } else {
                dm.setToDefaults();
            }
        } catch (Exception e) {
        }
        return dm;
    }

    /**
     * getDeviceDisplayWidth:获取设备的宽度
     *
     * @param context
     * @return
     * @author zhaolei
     */
    public static int getDeviceDisplayWidth(Context context) {

        int width = 0;
        DisplayMetrics displayMetrics = getDeviceDisplayMetrics(context);
        if (null != displayMetrics) {
            width = displayMetrics.widthPixels;
        }
        return width;
    }

    /**
     * getDeviceDisplayHeight:获取设备的高度
     *
     * @param context
     * @return
     * @author zhaolei
     */
    public static int getDeviceDisplayHeight(Context context) {
        int height = 0;
        DisplayMetrics displayMetrics = getDeviceDisplayMetrics(context);
        if (null != displayMetrics) {
            height = displayMetrics.heightPixels;
        }
        return height;
    }

    /**
     * getSystemAvaialbeMemorySize:获得系统可用内存信息. <br/>
     *
     * @param context
     * @return
     * @author adison
     */
    public static long getSystemAvaialbeMemory(Context context) {
        // 获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        // 获得系统可用内存，保存在MemoryInfo对象上
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);

        return memoryInfo.availMem;
    }


    /**
     * 根据屏幕宽度与密度计算GridView显示的列数， 最少为三列，并获取Item宽度
     */
    public static int getImageItemWidth(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        int cols = screenWidth / densityDpi;
        cols = cols < 3 ? 3 : cols;
        int columnSpace = (int) (2 * context.getResources().getDisplayMetrics().density);
        return (screenWidth - columnSpace * (cols - 1)) / cols;
    }

    public static int get3ColumnItemWidth(Context context) {
        int width = 0;
        width = getScreenWidth(context) / 3;
        return width;
    }

    public static int get3ColumnGoodsImageItemWidth(Context context) {
        int width = 0;
        width = get3ColumnItemWidth(context) - dp2px(context, 8 * 2);
        return width;
    }

    public static int get3ColumnGoodsImageItemHeight(Context context) {
        int height = 0;
        height = get3ColumnGoodsImageItemWidth(context) * 133 / 100;
        return height;
    }

    public static int getGoodsImageHeight(Context context) {
        return getGoodsImageWidth(context) * 1020 / 720;
    }

    public static int getGoodsImageWidth(Context context) {
        return getScreenWidth(context);
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context activity) {
        if (activity == null) {
            return 0;
        }
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        return screenWidth;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context activity) {
        if (activity == null) {
            return 0;
        }
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        return screenHeight;
    }

    /**
     * 获得状态栏的高度
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    @SuppressLint("NewApi")
    public static boolean isDeviceHasNavigationBar(Context context) {

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    public static int getNavigationBarHeight(Activity activity) {
        int height = 0;
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static String getAndroidID(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
