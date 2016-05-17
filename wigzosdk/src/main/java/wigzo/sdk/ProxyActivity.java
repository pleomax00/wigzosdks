package wigzo.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        Bundle extras = getIntent().getExtras();

        Class<? extends Activity> targetActivity = (Class<? extends Activity>) extras.get("targetActivity");
        String intentData = (String) extras.get("intentData");

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
