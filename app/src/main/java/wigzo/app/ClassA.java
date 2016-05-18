package wigzo.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.model.UserProfile;

/**
 * Created by wigzo on 17/5/16.
 */
public class ClassA extends Activity {
    Button go;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_a);
        Log.d("Called! :" , "onCreate method is called!   A");
        WigzoSDK sdk = WigzoSDK.getInstance();
        sdk.initializeWigzoData(this, "56065c5b-db30-4b89-bd76-0a9c2938c90b");

        go = (Button)findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                //Create the intent to start another activity
                Intent intent = new Intent(ClassA.this,ClassB.class);
                startActivity(intent);
            }
        });

        /*UserProfile user = new UserProfile("abc","abc","suyash@wigzo.com","wigzo.com");
        user.setPicturePath("/sdcard/Pictures/OGQ/pic.jpg");
        Map<String, String> customData = new HashMap<>();
        customData.put("key1","value1");
        customData.put("key2","value2");
        user.setCustomData(customData);
        user.saveUserProfile();
*/

    }
    public void startActivity(){
        Intent goIntent = new Intent(ClassA.this, ClassB.class);
        startActivity(goIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Called! :" , "onStart method is called!  A");
        WigzoSDK.getInstance().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Called! :" , "onResume method is called!   A");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Called! :" , "onRestart method is called!   A");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Called! :" , "onPause method is called!    A");
    }

    @Override
    protected void onStop() {
        WigzoSDK.getInstance().onStop();
        super.onStop();
        Log.d("Called! :" , "onStop method is called!  A");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Called! :" , "onDestroy method is called!  A");
    }
}
