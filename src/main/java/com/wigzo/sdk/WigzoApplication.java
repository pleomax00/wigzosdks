package com.wigzo.sdk;

import android.app.Application;
import android.content.Context;

/**
 * Created by wigzo on 27/4/17.
 */

public class WigzoApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        WigzoApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return WigzoApplication.context;
    }
}
