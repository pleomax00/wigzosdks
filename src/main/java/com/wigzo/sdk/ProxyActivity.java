package com.wigzo.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.helpers.StringUtils;
import com.wigzo.sdk.model.FcmOpen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Keep
public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        Bundle extras = getIntent().getExtras();

        Class<? extends Activity> targetActivity = (Class<? extends Activity>) extras.get("targetActivity");
        final String uuid = (String) extras.get("uuid");
        String intentData = (String) extras.get("intentData");
        String linkType = (String) extras.get("linkType");
        String link = (String) extras.get("link");
        final int campaignId = extras.getInt("campaignId");
        final int organizationId = extras.getInt("organizationId");

        if (StringUtils.isNotEmpty(uuid)) {
            final ScheduledExecutorService fcmReadWorker = Executors.newSingleThreadScheduledExecutor();
            final Context applicationContext = this;
            fcmReadWorker.schedule(new Runnable() {
                @Override
                public void run() {
                    FcmOpen fcmOpen = new FcmOpen(uuid, campaignId, organizationId);
                    FcmOpen.Operation operation = FcmOpen.Operation.saveOne(fcmOpen);
                    FcmOpen.editOperation(applicationContext, operation);
                }
            }, 0, TimeUnit.SECONDS);
        }

        if (linkType.equals("TARGET_ACTIVITY")) {
            Map<String, Object> intentDataMap = gson.fromJson(intentData, new TypeToken<HashMap<String, Object>>() {
            }.getType());

            Intent targetIntent = new Intent(this, targetActivity);

            if (null != intentDataMap) {
                for (Map.Entry<String, Object> entry : intentDataMap.entrySet()) {
                    if (entry.getValue() instanceof CharSequence) {
                        targetIntent.putExtra(entry.getKey(), (CharSequence) entry.getValue());
                    } else if (entry.getValue() instanceof Number) {
                        targetIntent.putExtra(entry.getKey(), (Number) entry.getValue());
                    } else if (entry.getValue() instanceof Boolean) {
                        targetIntent.putExtra(entry.getKey(), (Boolean) entry.getValue());
                    }
                }
            }
            startActivity(targetIntent);
            finish();
        } else if (linkType.equals("URL")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            startActivity(intent);
            finish();
        }
    }

    Context getProxyContext()
    {
        return ProxyActivity.this;
    }
}
