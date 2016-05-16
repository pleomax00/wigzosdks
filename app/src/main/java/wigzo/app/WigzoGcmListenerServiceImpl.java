package wigzo.app;

import android.app.Activity;

import wigzo.sdk.WigzoGcmListenerService;

/**
 * Created by ankit on 16/5/16.
 */
public class WigzoGcmListenerServiceImpl extends WigzoGcmListenerService {

    @Override
    protected Class<? extends Activity> getTargetActivity() {
        return PushCatcherActivity.class;
    }
}
