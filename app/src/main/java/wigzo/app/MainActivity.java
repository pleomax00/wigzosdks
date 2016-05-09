package wigzo.app;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.model.EventInfo;
import wigzo.sdk.model.UserProfile;

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
        WigzoSDK sdk = WigzoSDK.getInstance();
        sdk.onStart();
        sdk.initializeWigzoData(this, "dd968763-6537-43ae-a86c-9d3922ebf319");
        EventInfo eventInfo = new EventInfo("view","viewed");
        EventInfo.Metadata metadata = new EventInfo.Metadata("Iphone","Iphone 6SE");
        eventInfo.setMetadata(metadata);
        sdk.saveEvent(eventInfo);
        EventInfo eventInfo1 = new EventInfo("addToCart","Add To Cart");
        EventInfo.Metadata metadata1 = new EventInfo.Metadata("Galaxy S","Galaxy S");
        metadata1.setTags("Phone");
        eventInfo1.setMetadata(metadata1);
        sdk.saveEvent(eventInfo1);
        EventInfo eventInfo2 = new EventInfo("Bought","Bought");
        EventInfo.Metadata metadata2 = new EventInfo.Metadata("Laptop","Lenovo");
        metadata2.setTags("Lenovo Flexi");
        metadata2.setPrice(new BigDecimal(45000));
        eventInfo2.setMetadata(metadata2);
        sdk.saveEvent(eventInfo2);
        UserProfile user = new UserProfile("abc","abc","suyash@wigzo.com","wigzo.com");
        //user.setPicturePath("/sdcard/Pictures/OGQ/pic.jpg");
        Map<String, String> customData = new HashMap<>();
        customData.put("key1","value1");
        customData.put("key2","value2");
        user.setCustomData(customData);
        user.saveUserProfile();
        sdk.onStop();
        onDestroy();

    }
}
