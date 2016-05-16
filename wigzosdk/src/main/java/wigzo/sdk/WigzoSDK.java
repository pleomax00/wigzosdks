package wigzo.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import wigzo.sdk.helpers.Configuration;
import wigzo.sdk.helpers.ConnectionStream;
import wigzo.sdk.helpers.WigzoSharedStorage;
import wigzo.sdk.model.DeviceInfo;
import wigzo.sdk.model.EventInfo;

/**
 * This class is the public API for the Wigzo Android SDK.
 *  @author Minaz Ali
 */


public class WigzoSDK {

    private Context context;
   // private String deviceId;
    private String appKey;
    private String orgToken;
    private boolean enableLogging = true;
    private long startTime;
    private String emailId;
    private Gson gson;
    private String senderId;

    Class <? extends Activity> targetActivity;

    public String getSenderId() {
        return senderId;
    }


    /**
     * Static class which returns singleton instance of WigzoSDK
     */
    // see http://stackoverflow.com/questions/7048198/thread-safe-singletons-in-java
    private static class SingletonHolder {
        static final WigzoSDK instance = new WigzoSDK();
    }

    /**
     *
     * @return Context of the application installing the SDK
     */
    public synchronized Context getContext() {
        return this.context;
    }


    /**
     * Returns the WigzoSdk singleton.
     */
    public static synchronized WigzoSDK getInstance() {

        return SingletonHolder.instance;
    }

    private WigzoSDK(){
        int timer = Integer.parseInt(Configuration.TIME_DELAY.value);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                checkAndPushEvent();

            }},timer,timer, TimeUnit.SECONDS);
    }

//    private boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//        return resultCode == ConnectionResult.SUCCESS;
//    }

    public void gcmRegister() {
        Intent intent = new Intent(getContext(), WigzoRegistrationIntentService.class);
        getContext().startService(intent);
    }

    /**
     * Initializes the Wigzo SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * @param context Context of application installing sdk
     * @param orgToken Organization token
     *
     */
    public synchronized WigzoSDK initializeWigzoData(Context context, String orgToken) {

        if (context == null) {
            throw new IllegalArgumentException("valid context is required");
        }
        else{
            this.context = context;
        }
        if (orgToken == null || orgToken.length() == 0) {
            throw new IllegalArgumentException("Valid Organization Id is required!");
        }
        else {
            this.orgToken = orgToken;
        }

        this.gson = new Gson();

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        String storedAppKey = wigzoSharedStorage.getSharedStorage().getString(Configuration.APP_KEY.value,"");
        if(StringUtils.isEmpty(storedAppKey)){
            this.appKey = UUID.randomUUID().toString();
            wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.APP_KEY.value, this.appKey).apply();
        }else{
            this.appKey = storedAppKey;
        }
//        String storedDeviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value, "");
        Boolean initDataSynced = wigzoSharedStorage.getSharedStorage().getBoolean(Configuration.WIGZO_INIT_DATA_SYNC_FLAG_KEY.value, false);
        if(!(initDataSynced)) {
           String deviceId  = UUID.randomUUID().toString();
           wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.DEVICE_ID_KEY.value, deviceId).apply();
           final String userData = getUserIdentificationData();
           final String url = Configuration.BASE_URL.value + Configuration.INITIAL_DATA_URL.value;
           ExecutorService executorService = Executors.newSingleThreadExecutor();
           Future<Boolean> future = executorService.submit(new Callable<Boolean>(){
                public Boolean call()  {
                    String response = ConnectionStream.postRequest(url,userData);
                    if (null != response) {
                        Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                        }.getType());
                        if ("success".equals(jsonResponse.get("status"))) {
                            return true;
                        }
                    }
                    return false;
                }});
            try {
                if(future.get()){
                    wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.WIGZO_INIT_DATA_SYNC_FLAG_KEY.value, true).apply();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return this;
    }


    public synchronized WigzoSDK initializeWigzoData(Context context, String orgToken, String senderId, Class <? extends Activity> targetActivity){
        initializeWigzoData(context,orgToken);
        if(StringUtils.isNotEmpty(senderId)){
            this.senderId = senderId;
            this.targetActivity = targetActivity;
            gcmRegister();
        }else {
            throw new IllegalArgumentException("Valid Sender Id is required!");
        }
        return this;
    }


    /**
     * This method is used to store events(or Activities)
     * @param eventInfo instance of EventInfo
     *//*
    public synchronized void saveEvent(final EventInfo eventInfo) {

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        List<EventInfo> eventInfos = wigzoSharedStorage.getEventList();
        eventInfos.add(eventInfo);
        final String eventsStr = this.gson.toJson(eventInfos);
        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();

    }*/

    /**
     * Once the email id of user is obtained, this method is used to map email id to user if it was not mapped when UserProfile instance was created
     * @param emailId email id of user
     */
    public synchronized void mapEmail(final String emailId){

        boolean checkStatus = checkWigzoData();
        if(checkStatus) {
            Map<String, String> emailData = new HashMap<>();
            this.emailId = emailId;
            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
            wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EMAIL_KEY.value, emailId).apply();
            String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value,"");
            emailData.put("deviceId", deviceId);
            emailData.put("orgToken", this.orgToken);
            emailData.put("email", this.emailId);
            final String emailDataStr = this.gson.toJson(emailData);
            final String url = Configuration.BASE_URL.value + Configuration.EMAIL_DATA_URL.value;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    ConnectionStream.postRequest(url, emailDataStr);
                }
            });
        }else{

            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo initial data is not initiallized.Cannot send event information");
        }

    }

    /**
     * Method to send events to wigzo server
     */
    private synchronized void checkAndPushEvent(){

        boolean checkStatus = checkWigzoData();
        if(checkStatus) {

            Map<String, Object> eventData = new HashMap<>();
            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
            String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value,"");
            eventData.put("deviceId", deviceId);
            eventData.put("orgToken", this.orgToken);
            eventData.put("appKey",this.appKey);
            DeviceInfo deviceInfo = new DeviceInfo();
            eventData.put("deviceInfo", deviceInfo.getMetrics(this.context));
            List<EventInfo> eventInfos = wigzoSharedStorage.getEventList();
            if(!eventInfos.isEmpty()) {
                final String eventsStr = this.gson.toJson(eventInfos);
                wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();
                eventData.put("eventData", eventsStr);
                final String eventDataStr = this.gson.toJson(eventData);
                final String url = Configuration.BASE_URL.value + Configuration.EVENT_DATA_URL.value;
                ConnectionStream.postRequest(url, eventDataStr);
                List<EventInfo> newEvents = wigzoSharedStorage.getEventList();
                newEvents.removeAll(eventInfos);
                wigzoSharedStorage.getSharedStorage().edit().putString("WIGZO_EVENTS", gson.toJson(newEvents)).apply();

            }
        }else{

            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo SDK data is not initialized.Cannot send event information");
        }

    }

    /**
     * Method to prepare Map of User Identification data. Map contains deviceId and orgToken
     * @return
     */
    public String getUserIdentificationData(){


        Map<String , Object> userData = new HashMap<>();
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value,"");
        userData.put("deviceId",deviceId);
        userData.put("orgToken",this.orgToken);
        return this.gson.toJson(userData);
    }

    /**
     * This method is used to check if WigzoSdk is initialized or not before sending any request to wigzo server
     * @return
     */
    public boolean checkWigzoData(){
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value,"");
        if(StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(this.orgToken) || this.context == null){
            return false;
        }
        return true;
    }

    /**
     * Method to track session start time. This should be called when app starts and not in all activities
     */
    public synchronized void onStart() {
       //CrashDetails.inForeground();
        this.startTime = System.currentTimeMillis()/1000l;

    }

    /**
     * Method to track session end time. This should be called when app is about to close and not in all activities
     */
    public synchronized void onStop(){

        long duration = (System.currentTimeMillis()/1000l) - this.startTime;
        String durationStr = Long.toString(duration);
        boolean checkStatus = checkWigzoData();
        if(checkStatus) {
            final Map<String, String> sessionData = new HashMap<>();
            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
            String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value,"");
            sessionData.put("deviceId", deviceId);
            sessionData.put("orgToken", this.orgToken);
            sessionData.put("sessionData", durationStr);
            Gson gson = new Gson();
            final String sessionDataStr = gson.toJson(sessionData);
            final String url = Configuration.BASE_URL.value + Configuration.SESSION_DATA_URL.value;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    ConnectionStream.postRequest(url, sessionDataStr);
                }
            });
       /* WigzoSharedStorage storage = new WigzoSharedStorage(this.context);
        storage.getSharedStorage().edit().clear().commit();*/
        }
    }

    public synchronized boolean isLoggingEnabled() {
        return this.enableLogging;
    }

    public synchronized String getOrgToken() {
        return orgToken;
    }

    public synchronized String getAppKey() {
        return appKey;
    }

    public Class<? extends Activity> getTargetActivity() {
        return targetActivity;
    }

}
