package com.wigzo.sdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.wigzo.sdk.helpers.Configuration;
import com.wigzo.sdk.helpers.WigzoSharedStorage;

public class WigzoBaseActiviy extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        WigzoSDK.getInstance().appStatus("true", getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();

        WigzoSDK.getInstance().appStatus("false");
    }
}
