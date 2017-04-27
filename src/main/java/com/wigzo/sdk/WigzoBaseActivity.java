package com.wigzo.sdk;

import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;

@Keep
public class WigzoBaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        WigzoSDK.getInstance().setContext(this);
        WigzoSDK.getInstance().appStatus("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        WigzoSDK.getInstance().appStatus("false");
    }

    @Override
    protected void onStart() {
        super.onStart();
        WigzoSDK.getInstance().onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
        WigzoSDK.getInstance().onStop();
    }
}
