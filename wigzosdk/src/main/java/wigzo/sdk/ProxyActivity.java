package wigzo.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import wigzo.sdk.model.GcmOpen;
import wigzo.sdk.model.GcmRead;

public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        Bundle extras = getIntent().getExtras();

        Class<? extends Activity> targetActivity = (Class<? extends Activity>) extras.get("targetActivity");
        final String uuid = (String) extras.get("uuid");
        String intentData = (String) extras.get("intentData");

        if (StringUtils.isNotEmpty(uuid)) {
            final ScheduledExecutorService gcmReadWorker = Executors.newSingleThreadScheduledExecutor();
            final Context applicationContext = this;
            gcmReadWorker.schedule(new Runnable() {
                @Override
                public void run() {
                    GcmOpen gcmOpen = new GcmOpen(uuid);
                    GcmOpen.Operation operation = GcmOpen.Operation.saveOne(gcmOpen);
                    GcmOpen.editOperation(applicationContext, operation);
                }
            }, 0, TimeUnit.SECONDS);

        }

        Map<String, Object> intentDataMap = gson.fromJson(intentData, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        Intent targetIntent = new Intent(this, targetActivity);

        if (null != intentDataMap) {
            for (Map.Entry<String, Object> entry : intentDataMap.entrySet())
            {
                if (entry.getValue() instanceof CharSequence) {
                    targetIntent.putExtra(entry.getKey(), (CharSequence) entry.getValue());
                }
                else if (entry.getValue() instanceof Number) {
                    targetIntent.putExtra(entry.getKey(), (Number) entry.getValue());
                }
                else if (entry.getValue() instanceof Boolean) {
                    targetIntent.putExtra(entry.getKey(), (Boolean) entry.getValue());
                }
            }
        }
        startActivity(targetIntent);
    }
}
