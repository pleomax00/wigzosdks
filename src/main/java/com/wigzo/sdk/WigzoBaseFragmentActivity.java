package com.wigzo.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;

@Keep
public class WigzoBaseFragmentActivity extends FragmentActivity implements Application.ActivityLifecycleCallbacks {

    private WeakReference<Context> foregroundActivity;

    @Override
    protected void onResume() {
        super.onResume();
        WigzoSDK.getInstance().setContext(this);
        /*registerAppStatus(this);*/
        WigzoSDK.getInstance().appStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*WigzoSDK.getInstance().appStatus(false);*/
        foregroundActivity = null;
        WigzoSDK.getInstance().appStatus(false);
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

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        registerAppStatus(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String class_name_activity = activity.getClass().getCanonicalName();
        if (null != foregroundActivity && foregroundActivity.get().getClass().getCanonicalName().equals(class_name_activity)) {
            foregroundActivity = null;
        }
        registerAppStatus(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isOnForeground(@NonNull Context activity_cntxt) {
        return isOnForeground(activity_cntxt.getClass().getCanonicalName());
    }

    public boolean isOnForeground(@NonNull String activity_canonical_name) {
        if (foregroundActivity != null && foregroundActivity.get() != null) {
            return foregroundActivity.get().getClass().getCanonicalName().equals(activity_canonical_name);
        }
        return false;
    }

    private boolean registerAppStatus(Activity activity) {
        foregroundActivity=new WeakReference<Context>(activity);
        WigzoSDK.getInstance().setContext(this);
        boolean isOnForeground = isOnForeground(activity);
        WigzoSDK.getInstance().appStatus(isOnForeground);
        return isOnForeground;
    }
}