package com.wigzo.sdk.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.wigzo.sdk.WigzoApplication;

public class WigzoEnvironment {
    private static String channelId = "";
    private static String channelName = "";
    private static String channelDescription = "";
    private static String fcmSenderId = "";
    private static int channelImportance = 3;
    private static int notificationPriority = 0;
    private static boolean isNotificationAutoCancel = true;

    public static String getChannelId() {
        //SETTING CHANNEL ID
        if (null == manifestReader(Configuration.W_FCM_CHANNEL_ID.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_FCM_CHANNEL_ID.key).toString())) {
            channelId = Configuration.W_FCM_CHANNEL_ID.value;
        } else {
            channelId = manifestReader(Configuration.W_FCM_CHANNEL_ID.key).toString();
        }
        return channelId;
    }

    public static String getChannelName() {
        //SETTING CHANNEL NAME
        if (null == manifestReader(Configuration.W_FCM_CHANNEL_NAME.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_FCM_CHANNEL_NAME.key).toString())) {
            channelName = Configuration.W_FCM_CHANNEL_NAME.value;
        } else {
            channelName = manifestReader(Configuration.W_FCM_CHANNEL_NAME.key).toString();
        }
        return channelName;
    }

    public static String getChannelDescription() {
        //SETTING CHANNEL DESCRIPTION
        if (null == manifestReader(Configuration.W_FCM_CHANNEL_DESCRIPTION.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_FCM_CHANNEL_DESCRIPTION.key).toString())) {
            channelDescription = Configuration.W_FCM_CHANNEL_DESCRIPTION.value;
        } else {
            channelDescription = manifestReader(Configuration.W_FCM_CHANNEL_DESCRIPTION.key).toString();
        }
        return channelDescription;
    }

    public static int getChannelImportance() {
        //SETTING CHANNEL IMPORTANCE
        if (null == manifestReader(Configuration.W_FCM_CHANNEL_IMPORTANCE.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_FCM_CHANNEL_IMPORTANCE.key).toString())) {
            channelImportance = Integer.parseInt(Configuration.W_FCM_CHANNEL_IMPORTANCE.value);
        } else {
            channelImportance = (Integer) manifestReader(Configuration.W_FCM_CHANNEL_IMPORTANCE.key);
        }
        return channelImportance;
    }

    public static int getNotificationPriority() {
        //SETTING NOTIFICATION PRIORITY
        if (null == manifestReader(Configuration.W_NOTIFICATION_PRIORITY.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_NOTIFICATION_PRIORITY.key).toString())) {
            notificationPriority = Integer.parseInt(Configuration.W_NOTIFICATION_PRIORITY.value);
        } else {
            notificationPriority = (Integer) manifestReader(Configuration.W_NOTIFICATION_PRIORITY.key);
        }
        return notificationPriority;
    }

    public static boolean getNotificationAutoCancelBehaviour() {
        if (null == manifestReader(Configuration.W_IS_NOTIFICATION_AUTO_CANCEL.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_IS_NOTIFICATION_AUTO_CANCEL.key).toString())) {
            isNotificationAutoCancel = Boolean.parseBoolean(Configuration.W_IS_NOTIFICATION_AUTO_CANCEL.value);
        } else {
            isNotificationAutoCancel = (Boolean) manifestReader(Configuration.W_IS_NOTIFICATION_AUTO_CANCEL.key);
        }
        return isNotificationAutoCancel;
    }

    public static String getFCMSenderId() {
        //SETTING FCM SENDER ID
        if (null == manifestReader(Configuration.W_FCM_SENDER_ID.key)
                || StringUtils.isEmpty(manifestReader(Configuration.W_FCM_SENDER_ID.key).toString())) {
            fcmSenderId = "" + (Float) manifestReader(Configuration.W_FCM_SENDER_ID.key);
        } else {
            fcmSenderId = Configuration.W_FCM_SENDER_ID.value;
        }
        return fcmSenderId;
    }

    public static void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(getChannelId(), getChannelName(), getChannelImportance());
            channel.setDescription(getChannelDescription());
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = WigzoApplication.getInstance().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static Object manifestReader(String name) {
        Object value = "";
        try {
            ApplicationInfo applicationInfo = WigzoApplication
                    .getInstance()
                    .getPackageManager()
                    .getApplicationInfo(WigzoApplication.getInstance().getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            value = bundle.get(name);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("META_DATA", "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("META_DATA", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        return value;

    }
}
