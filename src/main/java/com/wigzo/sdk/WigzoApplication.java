package com.wigzo.sdk;

import android.app.Application;
import android.content.Context;

import com.wigzo.sdk.helpers.WigzoEnvironment;

/**
 * Created by wigzo on 27/4/17.
 */

public class WigzoApplication extends Application {

    private static Context context;
    private static WigzoApplication wigzoApplicationInstance = null;

    public void onCreate() {
        super.onCreate();
        wigzoApplicationInstance = this;
        WigzoApplication.context = getApplicationContext();
        WigzoEnvironment.createNotificationChannel();
    }

    public static WigzoApplication getInstance() {
        return wigzoApplicationInstance;
    }

    public static Context getAppContext() {
        return WigzoApplication.context;
    }
}
