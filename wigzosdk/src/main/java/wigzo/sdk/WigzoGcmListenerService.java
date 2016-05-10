package wigzo.sdk;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by ankit on 9/5/16.
 */
public class WigzoGcmListenerService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d("gcm", data.toString());
    }
}
