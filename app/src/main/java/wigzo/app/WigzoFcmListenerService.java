package wigzo.app;
import android.app.Activity;
import wigzo.sdk.AbstractWigzoFcmListenerService;

public class WigzoFcmListenerService extends AbstractWigzoFcmListenerService {
    @Override
    protected Class<? extends Activity> getTargetActivity() {
        return TargetActivity.class;
    }
}
