package wigzo.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.SSLContext;

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

    private SSLContext sslContext;
    private List<String> publicKeyPinCertificates;
    private boolean wigzoSdkInitialized = false;
    private Context context;
    private SharedPreferences sharedStorage = null;
    private static String serverUrl;
    private String deviceId;
    private String orgToken;
    private boolean enableLogging;
    private long startTime;


    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    public  void setContext(Context context) {
        this.context = context;
    }

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

    /**
     * Initializes the Wigzo SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     * @param context application context
     * @param serverURL URL of the Wigzo server to submit data to; use "https://analytics.wigzo.com" for Wigzo Cloud
     * @param orgToken    unique Id for organization provided by wigzo to organization
     * @return Wigzo instance for easy method chaining
     * @throws IllegalArgumentException if context, serverURL, appKey, or deviceID are invalid
     * @throws IllegalStateException if initializeWigzoData has previously been called with different values during the same application instance
     */
    public WigzoSDK initializeWigzoData(Context context, String serverURL, String orgToken, SSLContext sslContext) {
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
        if (!isValidURL(serverURL)) {
            throw new IllegalArgumentException("valid serverURL is required");
        }
        else{
            this.serverUrl = serverURL;
        }

        this.deviceId  = UUID.randomUUID().toString();
   /*     WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        this.sharedStorage = wigzoSharedStorage.getSharedStorage();
        sharedStorage.edit().putString(Configuration.DEVICE_ID_KEY.value, deviceId).apply();*/

        //TODO: Disabled only for testing. Enable it later
       /* if (sslContext == null ){
            throw new IllegalArgumentException("Valid SSL Context is required!");
        }else {*/
            this.sslContext = sslContext;
        //}

        if(!this.wigzoSdkInitialized){
            final String userData = getUserIdentificationData();
            final String url = serverURL + "/androidsdk/getinitialdata";
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executorService.submit(new Callable<Boolean>(){
                public Boolean call()  {
                    Boolean success = ConnectionStream.sendData(url,userData);
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


    public void pushEvent(EventInfo eventInfo) {
            //TODO: add null checks for url,sslcontext
        /**
         * 1. call getEventList() to get already stored eventInfos
         * 2. append this eventInfo to the list
         * 3. if eventInfo size is greater than threshold, send data to server
         * 4. if send successful, remove eventInfo data from shared storage
         */
        Map<String,Object> eventData = new HashMap<>();
        eventData.put("DeviceId",this.deviceId);
        eventData.put("OrgToken",this.orgToken);
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        this.sharedStorage = wigzoSharedStorage.getSharedStorage();
        List<EventInfo> eventInfos = wigzoSharedStorage.getEventList();
        eventInfos.add(eventInfo);
        Gson gson = new Gson();
        final String eventsStr = gson.toJson(eventInfos);
        sharedStorage.edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();
        if(eventInfos.size() >= Integer.parseInt(Configuration.EVENT_QUEUE_SIZE_THRESHOLD.value)){
            eventData.put("EventData",eventsStr);
            final String eventDataStr = gson.toJson(eventData);
            final String url = this.serverUrl + "/androidsdk/geteventdata";
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<?> future = executorService.submit(new Runnable(){
                public void run()  {
                    ConnectionStream.sendData(url, eventDataStr);

                }});
            sharedStorage.edit().putString("WIGZO_EVENTS", "").apply();

        }

    }

    public String getUserIdentificationData(){

        Gson gson = new Gson();
        Map<String , Object> userData = new HashMap<>();
        userData.put("DeviceId",this.deviceId);
        userData.put("OrgToken",this.orgToken);
        DeviceInfo deviceInfo = new DeviceInfo();
        userData.put("DeviceInfo", deviceInfo.getMetrics(this.context));
        /*if(this.senderId != null){
            set senderid as well
        }*/
        return gson.toJson(userData);
    }

    /**
     * Utility method for testing validity of a URL.
     */
    static boolean isValidURL(final String urlStr) {
        boolean validURL = false;
        if (urlStr != null && urlStr.length() > 0) {
            try {
                new URL(urlStr);
                validURL = true;
            }
            catch (MalformedURLException e) {
                validURL = false;
            }
        }
        return validURL;
    }

    public synchronized void onStart() {
       //CrashDetails.inForeground();
        this.startTime = System.currentTimeMillis()/1000l;

    }

    public void onStop(){
        long duration = (System.currentTimeMillis()/1000l) - this.startTime;

    }

    public boolean isLoggingEnabled() {
        return this.enableLogging;
    }
    public SSLContext getSslContext() {
        return this.sslContext;
    }

    public List<String> getPublicKeyPinCertificates() {
        return this.publicKeyPinCertificates;
    }

}
