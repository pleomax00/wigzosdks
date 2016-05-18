package wigzo.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.model.EventInfo;
import wigzo.sdk.model.UserProfile;

/**
 * Created by wigzo on 17/5/16.
 */
public class ClassB extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_b);
        Log.d("Called! :" , "onCreate method is called!   B");
        EventInfo eventInfo = new EventInfo("view","viewed");
        EventInfo.Metadata metadata = new EventInfo.Metadata("1","Iphone","Iphone 6SE",null);
        eventInfo.setMetadata(metadata);
        eventInfo.saveEvent();

        EventInfo eventInfo1 = new EventInfo("addToCart","Add To Cart");
        EventInfo.Metadata metadata1 = new EventInfo.Metadata("2","Galaxy S","Galaxy S",null);
        metadata1.setTags("Phone");
        eventInfo1.setMetadata(metadata1);
        eventInfo1.saveEvent();
        UserProfile user = new UserProfile("abc","abc","suyash@wigzo.com","wigzo.com");
        user.setPicturePath("/sdcard/Pictures/OGQ/pic.jpg");
        Map<String, String> customData = new HashMap<>();
        customData.put("key1","value1");
        customData.put("key2","value2");
        user.setCustomData(customData);
        user.saveUserProfile();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Called! :" , "onStart method is called!  B");
        WigzoSDK.getInstance().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Called! :" , "onResume method is called!   B");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Called! :" , "onRestart method is called!   B");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Called! :" , "onPause method is called!    B");
    }

    @Override
    protected void onStop() {
        WigzoSDK.getInstance().onStop();
        super.onStop();
        Log.d("Called! :" , "onStop method is called!  B");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Called! :" , "onDestroy method is called!  B");
    }
}
