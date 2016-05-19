package wigzo.app;
import android.app.Activity;
import wigzo.sdk.AbstractWigzoGcmListenerService;

public class WigzoGcmListenerService extends AbstractWigzoGcmListenerService {
    @Override
    protected Class<? extends Activity> getTargetActivity() {
        return TargetActivity.class;
    }
}
