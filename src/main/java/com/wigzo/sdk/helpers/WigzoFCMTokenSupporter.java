package com.wigzo.sdk.helpers;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.WigzoFcmListenerService;
import com.wigzo.sdk.WigzoSDK;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.wigzo.sdk.WigzoFcmListenerService.refreshedToken;

public class WigzoFCMTokenSupporter {

    /*send token to WIGZO server*/
    public static void sendRegistrationToServer(String token) {
        final Gson gson = new Gson();
        if (null == WigzoSDK.getInstance().getContext()) return;

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(WigzoSDK.getInstance().getContext());
        if (null == wigzoSharedStorage) return;

        SharedPreferences sharedPreferences = wigzoSharedStorage.getSharedStorage();
        if (null == sharedPreferences) return;

        // Send to server only when the token is not yet sent
        if (!sharedPreferences.getBoolean(Configuration.SENT_FCM_TOKEN_TO_SERVER.value, false)) {

            //hashmap to store user data to send it to FCM server
            Map<String, Object> eventData = new HashMap<>();

            //store data in eventData
            eventData.put("registrationId", token);
            eventData.put("orgtoken", WigzoSDK.getInstance().getOrgToken());

            //Convert eventData to json string
            final String eventDataStr = gson.toJson(eventData);

            //Endpoint Url
            final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                            + Configuration.FCM_REGISTRATION_URL.value, Configuration.SITE_ID.value
                    , WigzoSDK.getInstance().getOrgToken());

            if (StringUtils.isNotEmpty(WigzoSDK.getInstance().getOrgToken())) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                    public Boolean call() {
                        //Post data to server
                        String response = ConnectionStream.postRequest(url, eventDataStr);

                        //Check if post request returned success if the response is not null
                        if (null != response && StringUtils.isJsonString(response)) {
                            try {
                                Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                                }.getType());
                                if ("success".equals(jsonResponse.get("status"))) {
                                    WigzoFcmListenerService.isSentToServer = true;
                                    return true;
                                }
                            } catch (Exception e) {
                                return false;
                            }
                        }
                        return false;
                    }
                });

                try {
                    //if post request was successful save the Synced data flag as true in shared preferences
                    if (future.get()) Log.i("Status ", "Token successfully sent to server");
                    else Log.e("Status ", "Failed to send token to server. Please verify things are right else contact Wigzo for troubleshooting.");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*MAP DEVICE ID WITH THE TOKEN AT WIGZO SERVER */
    public static void mapFcmToDeviceId(String token) {
        final Gson gson = new Gson();
        if (null == WigzoSDK.getInstance().getContext()) return;

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(WigzoSDK.getInstance().getContext());
        if (null == wigzoSharedStorage) return;

        final SharedPreferences sharedPreferences = wigzoSharedStorage.getSharedStorage();
        if (null == sharedPreferences) return;

        Boolean initDataSynced = sharedPreferences.getBoolean(Configuration.WIGZO_INIT_DATA_SYNC_FLAG_KEY.value, false);

        Boolean isFcmDeviceMapped = sharedPreferences.getBoolean(Configuration.FCM_DEVICE_MAPPED.value, false);

        if (initDataSynced && !isFcmDeviceMapped) {

            Map<String, Object> eventData = new HashMap<>();

            String deviceId = sharedPreferences.getString(Configuration.DEVICE_ID_KEY.value, "");

            eventData.put("registrationId", token);
            eventData.put("orgtoken", WigzoSDK.getInstance().getOrgToken());
            eventData.put("deviceId", deviceId);

            final String eventDataStr = gson.toJson(eventData);

            final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                            + Configuration.FCM_DEVICE_MAPPING_URL.value, Configuration.SITE_ID.value
                    , WigzoSDK.getInstance().getOrgToken());

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                public Boolean call() {
                    //Post data to server
                    String response = ConnectionStream.postRequest(url, eventDataStr);
                    if (null != response) {
                        Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                        }.getType());
                        if ("success".equals(jsonResponse.get("status"))) {
                            sharedPreferences.edit().putBoolean(Configuration.FCM_DEVICE_MAPPED.value, true);
                            return true;
                        }
                        return false;
                    }
                    return false;
                }
            });

            try {
                //if post request was successful save the Synced data flag as true in shared preferences
                if (future.get()) Log.d("Mapping" , "Mapped FCM to device");
                else Log.d("Mapping" , "Could not map FCM to device");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Subscribe to any FCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token FCM token
     * @throws IOException if unable to reach the FCM PubSub service
     */
    // [START subscribe_topics]
    public static void subscribeTopics(String token) throws IOException {
        FirebaseMessaging.getInstance().subscribeToTopic("orgsubscribe-" + WigzoSDK.getInstance().getOrgToken());
    }
    // [END subscribe_topics]

    public static String getGeneratedToken() {
        if (StringUtils.isEmpty(refreshedToken)) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> future = executorService.submit(new Callable<String>() {
                public String call() {
                    refreshedToken = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();
                    Log.d("REFRESHEDTOKEN", refreshedToken);
                    return refreshedToken;
                }
            });

            try {
                if (StringUtils.isNotEmpty(future.get())) {
                    refreshedToken = future.get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                refreshedToken = "";
            } catch (ExecutionException e) {
                e.printStackTrace();
                refreshedToken = "";
            }
        }
        return refreshedToken;
    }
}
