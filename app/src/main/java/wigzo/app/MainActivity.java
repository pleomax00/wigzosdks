package wigzo.app;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.model.EventInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        WigzoSDK sdk = WigzoSDK.getSharedInstance();
        sdk.onStart();
        sdk.initializeWigzoData(this, "2345");
        EventInfo eventInfo = new EventInfo("view","viewed");
        EventInfo.Metadata metadata = new EventInfo.Metadata("Iphone","Iphone 6SE");
        eventInfo.setMetadata(metadata);
        sdk.pushEvent(eventInfo);
        sdk.onStop();
        onDestroy();

    }
}
