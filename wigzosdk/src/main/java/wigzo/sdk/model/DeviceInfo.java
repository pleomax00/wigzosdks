package wigzo.sdk.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.helpers.Configuration;

/**
 * Created by wigzo on 28/4/16.
 */
public class DeviceInfo {

    //private static final String TAG = "DeviceId";
    private String id;
    public DeviceInfo(){}

    protected void setId(String id) {
        if (WigzoSDK.getSharedInstance().isLoggingEnabled()) {
            Log.w(Configuration.DEVICE_ID_TAG.value, "Device ID is " + id );
        }
        this.id = id;
    }


    /**
     * Returns the display name of the current operating system.
     */
    static String getOS() {
        return "Android";
    }

    /**
     * Returns the current operating system version as a displayable string.
     */
    static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * Returns the current device model.
     */
    static String getDevice() {
        return android.os.Build.MODEL;
    }

    /**
     * Returns the non-scaled pixel resolution of the current default display being used by the
     * WindowManager in the specified context.
     * @param context context to use to retrieve the current WindowManager
     * @return a string in the format "WxH", or the empty string "" if resolution cannot be determined
     */
    static String getResolution(final Context context) {
        // user reported NPE in this method; that means either getSystemService or getDefaultDisplay
        // were returning null, even though the documentation doesn't say they should do so; so now
        // we catch Throwable and return empty string if that happens
        String resolution = "";
        try {
            final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            final Display display = wm.getDefaultDisplay();
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            resolution = metrics.widthPixels + "x" + metrics.heightPixels;
        }
        catch (Throwable t) {
            if (WigzoSDK.getSharedInstance().isLoggingEnabled()) {
                Log.i(Configuration.WIGZO_SDK_TAG.value, "Device resolution cannot be determined");
            }
        }
        return resolution;
    }

    /**
     * Maps the current display density to a string constant.
     * @param context context to use to retrieve the current display metrics
     * @return a string constant representing the current display density, or the
     *         empty string if the density is unknown
     */
    static String getDensity(final Context context) {
        String densityStr = "";
        final int density = context.getResources().getDisplayMetrics().densityDpi;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                densityStr = "LDPI";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                densityStr = "MDPI";
                break;
            case DisplayMetrics.DENSITY_TV:
                densityStr = "TVDPI";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                densityStr = "HDPI";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                densityStr = "XHDPI";
                break;
            case DisplayMetrics.DENSITY_400:
                densityStr = "XMHDPI";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                densityStr = "XXHDPI";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                densityStr = "XXXHDPI";
                break;
        }
        return densityStr;
    }

    /**
     * Returns the display name of the current network operator from the
     * TelephonyManager from the specified context.
     * @param context context to use to retrieve the TelephonyManager from
     * @return the display name of the current network operator, or the empty
     *         string if it cannot be accessed or determined
     */
    static String getCarrier(final Context context) {
        String carrier = "";
        final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            carrier = manager.getNetworkOperatorName();
        }
        if (carrier == null || carrier.length() == 0) {
            carrier = "";
            if (WigzoSDK.getSharedInstance().isLoggingEnabled()) {
                Log.i(Configuration.WIGZO_SDK_TAG.value, "No carrier found");
            }
        }
        return carrier;
    }

    /**
     * Returns the current locale (ex. "en_US").
     */
    static String getLocale() {
        final Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * Returns the application version string stored in the specified
     * context's package info versionName field, or "1.0" if versionName
     * is not present.
     */
    static String getAppVersion(final Context context) {
        String result = Configuration.DEFAULT_SDK_VERSION.value;
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            if (WigzoSDK.getSharedInstance().isLoggingEnabled()) {
                Log.i(Configuration.WIGZO_SDK_TAG.value, "No app version found");
            }
        }
        return result;
    }

    /**
     * Returns the package name of the app that installed this app
     */
    static String getStore(final Context context) {
        String result = "";
        if(android.os.Build.VERSION.SDK_INT >= 3 ) {
            try {
                result = context.getPackageManager().getInstallerPackageName(context.getPackageName());
            } catch (Exception e) {
                if (WigzoSDK.getSharedInstance().isLoggingEnabled()) {
                    Log.i(Configuration.WIGZO_SDK_TAG.value, "Can't get Installer package");
                }
            }
            if (result == null || result.length() == 0) {
                result = "";
                if (WigzoSDK.getSharedInstance().isLoggingEnabled()) {
                    Log.i(Configuration.WIGZO_SDK_TAG.value, "No store found");
                }
            }
        }
        return result;
    }

    /**
     * Returns a URL-encoded JSON string containing the device metrics
     * to be associated with a begin session event.
     * See the following link for more info:
     * https://wigzo/resources/reference/server-api
     */
    public String getMetrics(final Context context) {
        Map<String,Object> deviceInfo = new HashMap<>();

        deviceInfo.put("device", getDevice());
        deviceInfo.put("os", getOS());
        deviceInfo.put("osVersion", getOSVersion());
        deviceInfo.put("carrier", getCarrier(context));
        deviceInfo.put("resolution", getResolution(context));
        deviceInfo.put("density", getDensity(context));
        deviceInfo.put("locale", getLocale());
        deviceInfo.put("appVersion", getAppVersion(context));
        deviceInfo.put("installingApp", getStore(context));
        Gson gson = new Gson();

        String result = gson.toJson(deviceInfo);

        try {
            result = java.net.URLEncoder.encode(result, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // should never happen because Android guarantees UTF-8 support
        }

        return result;
    }


}
