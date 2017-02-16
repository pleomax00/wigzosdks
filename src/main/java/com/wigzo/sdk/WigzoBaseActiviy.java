package com.wigzo.sdk;

import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;

public class WigzoBaseActiviy extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        WigzoSDK.getInstance().setContext(this);
        WigzoSDK.getInstance().appStatus("true", getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        WigzoSDK.getInstance().appStatus("false");
    }
}
