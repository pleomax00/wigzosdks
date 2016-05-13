/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wigzo.sdk;

import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.iid.InstanceIDListenerService;

import wigzo.sdk.helpers.Configuration;
import wigzo.sdk.helpers.WigzoSharedStorage;

public class WigzoInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(WigzoSDK.getInstance().getContext());
        SharedPreferences sharedPreferences = wigzoSharedStorage.getSharedStorage();
        // Setting SENT_GCM_TOKEN_TO_SERVER to false since we want to refresh the token.
        sharedPreferences.edit().putBoolean(Configuration.SENT_GCM_TOKEN_TO_SERVER.value, false).apply();
        sharedPreferences.edit().putBoolean(Configuration.GCM_DEVICE_MAPPED.value, false).apply();

        Intent intent = new Intent(this, WigzoRegistrationIntentService.class);
        startService(intent);
    }
    // [END refresh_token]
}
