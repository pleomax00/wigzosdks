package wigzo.app;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.math.BigDecimal;

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
        sdk.initializeWigzoData(this, "dd968763-6537-43ae-a86c-9d3922ebf319");
        EventInfo eventInfo = new EventInfo("view","viewed");
        EventInfo.Metadata metadata = new EventInfo.Metadata("Iphone","Iphone 6SE");
        eventInfo.setMetadata(metadata);
        sdk.pushEvent(eventInfo);
        EventInfo eventInfo1 = new EventInfo("addToCart","Add To Cart");
        EventInfo.Metadata metadata1 = new EventInfo.Metadata("Galaxy S","Galaxy S");
        metadata1.setTags("Phone");
        eventInfo1.setMetadata(metadata1);
        sdk.pushEvent(eventInfo1);
        EventInfo eventInfo2 = new EventInfo("Bought","Bought");
        EventInfo.Metadata metadata2 = new EventInfo.Metadata("Laptop","Lenovo");
        metadata2.setTags("Lenovo Flexi");
        metadata2.setPrice(new BigDecimal(45000));
        eventInfo2.setMetadata(metadata2);
        sdk.pushEvent(eventInfo2);
        sdk.mapEmail("suyash@wigzo.com");
        sdk.onStop();
        onDestroy();

    }
}
