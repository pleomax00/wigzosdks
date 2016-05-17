package wigzo.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import wigzo.sdk.WigzoSDK;

/**
 * Created by wigzo on 17/5/16.
 */
public class ClassB extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_b);
        Log.d("Called! :" , "onCreate method is called!   B");

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
