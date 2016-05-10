package wigzo.sdk;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by ankit on 9/5/16.
 */
public class WigzoInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        WigzoSDK sdk = WigzoSDK.getSharedInstance();
        sdk.gcmSubscribe();
    }
}
