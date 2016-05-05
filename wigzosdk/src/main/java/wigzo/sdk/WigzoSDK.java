package wigzo.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
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
 * Created by wigzo on 28/4/16.
 */

/**
 * This class is the public API for the Wigzo Android SDK.
 */

//TODO :

/**
 * 1. add relevant getters and setters for clients to call/use
 * 2. implement Singleton pattern
 * 3. implement initializeWigzoData
 * 4. implement call to send intial user data
 * 5. logic to handle events
 */


public class WigzoSDK {

    private boolean wigzoSdkInitialized = false;
    private Context context;
    private SharedPreferences sharedStorage = null;
    private String deviceId;
    private String orgToken;
    private boolean enableLogging = true;
    private long startTime;




    /**
     * Enum used in Wigzo.initMessaging() method which controls what kind of
     * app installation it is. Later (in Wigzo Dashboard or when calling Wigzo API method),
     * you'll be able to choose whether you want to send a message to test devices,
     * or to production ones.
     */

    public static enum WigzoMessagingMode {
        TEST,
        PRODUCTION,
    }

    // see http://stackoverflow.com/questions/7048198/thread-safe-singletons-in-java
    private static class SingletonHolder {
        static final WigzoSDK instance = new WigzoSDK();
    }

    public Context getContext() {
        return this.context;
    }


    /**
     * Returns the Wigzo singleton.
     */
    public static WigzoSDK getSharedInstance() {

        return SingletonHolder.instance;
    }

    WigzoSDK(){
        int timer = Integer.parseInt(Configuration.TIME_DELAY.value);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                checkAndPushEvent();

            }},timer,timer, TimeUnit.SECONDS);
    }

    /**
     * Initializes the Wigzo SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * @param context application context
     * @param orgToken    unique Id for organization provided by wigzo to organization
     * @return Wigzo instance for easy method chaining
     * @throws IllegalArgumentException if context, serverURL, appKey, or deviceID are invalid
     * @throws IllegalStateException if initializeWigzoData has previously been called with different values during the same application instance
     */
    public WigzoSDK initializeWigzoData(Context context, String orgToken) {

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

        /**
         * 1. check if deviceId is already generated.If not generate device Id and store it in sharedpreference
         * 2. create user mapping data to be send to wigzo
         * 3. create thread and send data to wigzo.
         * 4. set flag wigzoSdkInitialized if data is sent successfully
         */



            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        this.sharedStorage = wigzoSharedStorage.getSharedStorage();
        String storedDeviceId = this.sharedStorage.getString(Configuration.DEVICE_ID_KEY.value, "");
        if(StringUtils.isEmpty(storedDeviceId)){
           this.deviceId  = UUID.randomUUID().toString();
           this.sharedStorage.edit().putString(Configuration.DEVICE_ID_KEY.value, deviceId).apply();
           final String userData = getUserIdentificationData();
           final String url = Configuration.BASE_URL.value + Configuration.INITIAL_DATA_URL.value;
           ExecutorService executorService = Executors.newSingleThreadExecutor();
           Future<Boolean> future = executorService.submit(new Callable<Boolean>(){
                public Boolean call()  {
                    Boolean success = ConnectionStream.postRequest(url,userData);
                    return success;
                }});
            try {
                if(future.get()){
                    this.wigzoSdkInitialized = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else if(!wigzoSdkInitialized){
            this.deviceId = storedDeviceId;
            final String userData = getUserIdentificationData();
            final String url = Configuration.BASE_URL.value + Configuration.INITIAL_DATA_URL.value;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executorService.submit(new Callable<Boolean>(){
                public Boolean call()  {
                    Boolean success = ConnectionStream.postRequest(url,userData);
                    return success;
                }});
            try {
                if(future.get()){
                    this.wigzoSdkInitialized = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return this;
    }


    public void pushEvent(final EventInfo eventInfo) {

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        this.sharedStorage = wigzoSharedStorage.getSharedStorage();
        List<EventInfo> eventInfos = wigzoSharedStorage.getEventList();
        eventInfos.add(eventInfo);
        Gson gson = new Gson();
        final String eventsStr = gson.toJson(eventInfos);
        sharedStorage.edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();

    }

    private void checkAndPushEvent(){

        boolean checkStatus = checkWigzoData();
        if(checkStatus) {

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("DeviceId", this.deviceId);
            eventData.put("OrgToken", this.orgToken);
            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
            this.sharedStorage = wigzoSharedStorage.getSharedStorage();
            List<EventInfo> eventInfos = wigzoSharedStorage.getEventList();
            if(!eventInfos.isEmpty()) {

                Gson gson = new Gson();
                final String eventsStr = gson.toJson(eventInfos);
                sharedStorage.edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();
                eventData.put("EventData", eventsStr);
                final String eventDataStr = gson.toJson(eventData);
                final String url = Configuration.BASE_URL.value + Configuration.EVENT_DATA_URL.value;
                sharedStorage.edit().putString("WIGZO_EVENTS", "").apply();
                ConnectionStream.postRequest(url, eventDataStr);
            }
        }else{

            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo initial data is not initiallized.Cannot send event information");
        }

    }

    public String getUserIdentificationData(){

        Gson gson = new Gson();
        Map<String , Object> userData = new HashMap<>();
        userData.put("DeviceId",this.deviceId);
        userData.put("OrgToken",this.orgToken);
        DeviceInfo deviceInfo = new DeviceInfo();
        userData.put("DeviceInfo", deviceInfo.getMetrics(this.context));
        //TODO:
        /*if(this.senderId != null){
            set senderid as well
        }*/
        return gson.toJson(userData);
    }

    public boolean checkWigzoData(){

        if(StringUtils.isEmpty(this.deviceId) || StringUtils.isEmpty(this.orgToken) || this.context == null){
            return false;
        }
        return true;
    }

    public synchronized void onStart() {
       //CrashDetails.inForeground();
        this.startTime = System.currentTimeMillis()/1000l;

    }

    public void onStop(){

        long duration = (System.currentTimeMillis()/1000l) - this.startTime;
        WigzoSharedStorage storage = new WigzoSharedStorage(this.context);
        storage.getSharedStorage().edit().clear().commit();

    }

    public boolean isLoggingEnabled() {
        return this.enableLogging;
    }

}
