package com.wigzo.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Keep;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.helpers.Configuration;
import com.wigzo.sdk.helpers.ConnectionStream;
import com.wigzo.sdk.helpers.StringUtils;
import com.wigzo.sdk.helpers.WigzoSharedStorage;
import com.wigzo.sdk.helpers.WigzoUrlWrapper;
import com.wigzo.sdk.model.DeviceInfo;
import com.wigzo.sdk.model.EventInfo;
import com.wigzo.sdk.model.FcmOpen;
import com.wigzo.sdk.model.FcmRead;

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

import static com.wigzo.sdk.helpers.WigzoFCMTokenSupporter.getGeneratedToken;
import static com.wigzo.sdk.helpers.WigzoFCMTokenSupporter.mapFcmToDeviceId;
import static com.wigzo.sdk.helpers.WigzoFCMTokenSupporter.sendRegistrationToServer;

/**
 * This class is the public API for the Wigzo Android SDK.
 *
 * @author Rihan Husain
 */
@Keep
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
    private SharedPreferences wigzoSharedStorageForAppliLifeCycle;

    /**
     * Getter for Sender Id
     *
     * @return Sender Id
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Static class which returns singleton instance of WigzoSDK
     */
    private static class SingletonHolder {
        static final WigzoSDK instance = new WigzoSDK();
    }

    /**
     * Getter for Context
     *
     * @return Context of the application installing the SDK
     */
    public synchronized Context getContext() {
        if (null == this.context)
            this.context = WigzoApplication.getAppContext();

        return this.context ;
    }

    protected synchronized void setContext(Context context) {
        this.context = context;
    }

    /**
     * Returns the WigzoSdk singleton.
     */
    public static synchronized WigzoSDK getInstance() {

        return SingletonHolder.instance;
    }

    /**
     * Private constructor which starts a scheduled thread to send event data. This scheduler also try to send Uer profile and email
     * information in case it was not sent when created.
     */
    private WigzoSDK() {
        int timer = Integer.parseInt(Configuration.TIME_DELAY.value);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                checkAndPushEvent();
                checkAndSendUserProfile();
                checkAndSendEmail();
                sendFcmRead();
                sendFcmOpen();
            }
        }, timer, timer, TimeUnit.SECONDS);
    }

    private void fcmRegister() {
        Intent intent = new Intent(getContext(), WigzoFcmListenerService.class);
        getContext().startService(intent);
    }

    /**
     * Initializes the Wigzo SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     *
     * @param context  Context of application installing sdk
     * @param orgToken Organization token
     * @return Instance of WigzoSDK
     * @throws IllegalStateException if either Context is missing or orgToken is missing
     */

    public synchronized WigzoSDK initializeWigzoData(Context context, String orgToken) {

        if (context == null) {
            throw new IllegalArgumentException("valid context is required");
        } else {
            this.context = context;
        }
        if (orgToken == null || orgToken.length() == 0) {
            throw new IllegalArgumentException("Valid Organization Id is required!");
        } else {
            this.orgToken = orgToken;
        }

        this.gson = new Gson();

        //Initialise shared preferences to store data in mobile for future use
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context == null ? getContext() : context);

        //Get AppKey from Shared preferences (will not exist on first run)
        String storedAppKey = wigzoSharedStorage.getSharedStorage().getString(Configuration.APP_KEY.value, "");

        //Save org token
        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.ORG_TOKEN_KEY.value, orgToken).apply();

        //if stored key does not exist then assign a new UUID AppKey
        if (StringUtils.isEmpty(storedAppKey)) {

            //Assign UUID to AppKey
            this.appKey = UUID.randomUUID().toString();

            //Save AppKey in Shared Preferences
            wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.APP_KEY.value, this.appKey).apply();
        }
        //if AppKey already exists then assign AppKey from Shared Preferences
        else {
            this.appKey = storedAppKey;
        }

        //get Device location and save in shared storage
        getDeviceLocation();

        //Boolean to check if data has already been synced
        Boolean initDataSynced = wigzoSharedStorage.getSharedStorage().getBoolean(Configuration.WIGZO_INIT_DATA_SYNC_FLAG_KEY.value, false);

        if (!(initDataSynced)) {

            //Assign Device ID
            String deviceId = UUID.randomUUID().toString();

            //Store Device Id in Shared Preferences
            wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.DEVICE_ID_KEY.value, deviceId).apply();

            //Get User Data json string to send to Wigzo server
            final String userData = getDeviceIdentificationData();

            //Url to send user data (BASE_URL("http://minaz.wigzoes.com"), INITIAL_DATA_URL("/androidsdk/getinitialdata"))
            final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                    + Configuration.INITIAL_DATA_URL.value, Configuration.SITE_ID.value
                    , getOrgToken());

            //Initialise Executor Service to send user data to server
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                public Boolean call() {
                    //Post data to server
                    String response = ConnectionStream.postRequest(url, userData);

                    //Check if post request returned success if the response is not null
                    if (null != response && StringUtils.isJsonString(response)) {
                        Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                        }.getType());

                        if ("success".equals(jsonResponse.get("status"))) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            try {

                //if post request was successful save the Synced data flag as true in shared preferences
                if (future.get()) {
                    wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.WIGZO_INIT_DATA_SYNC_FLAG_KEY.value, true).apply();
                    if (StringUtils.isNotEmpty(getGeneratedToken())) {
                        mapFcmToDeviceId(getGeneratedToken());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    /**
     * Initializes the Wigzo SDK. Call from your main Activity's onCreate() method.
     * Must be called before other SDK methods can be used.
     *
     * @param context  Context of application installing sdk
     * @param orgToken Organization token
     * @param senderId
     * @return instance of WigzoSDK
     * @throws IllegalStateException if either Context/orgToken/senderId is missing
     */

    //initializeWigzoData method overload if fcm id is passed
    public synchronized WigzoSDK initializeWigzoData(Context context, String orgToken, String senderId) {

        //first jnitialise in the normal manner
        initializeWigzoData(context, orgToken);

        //if token is not empty then store the token as sender id
        if (StringUtils.isNotEmpty(senderId)) {

            this.senderId = senderId;

            //Start Registration service
            fcmRegister();

        } else {
            throw new IllegalArgumentException("Valid Sender Id is required!");
        }
        return this;
    }


    /**

     * This method is used to store events(or Activities)
     * @param eventInfo instance of EventInfo
     *//*
    public synchronized void saveOne(final EventInfo eventInfo) {

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        List EventInfo eventInfos = wigzoSharedStorage.getEventList();
        eventInfos.add(eventInfo);
        final String eventsStr = this.gson.toJson(eventInfos);
        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();

    }*/

    /**
     * Once the email id of user is obtained, this method is used to map email id to user if it was not mapped when UserProfile instance was created
     *
     * @param emailId email id of user
     */
    public synchronized void mapEmail(final String emailId) {

        boolean checkStatus = checkWigzoData();
        if (checkStatus) {
            Map<String, String> emailData = new HashMap<>();
            this.emailId = emailId;
            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
            String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value, "");
            String appKey = wigzoSharedStorage.getSharedStorage().getString(Configuration.APP_KEY.value, "");
            emailData.put("deviceId", deviceId);
            emailData.put("appKey", appKey);
            emailData.put("orgToken", this.orgToken);
            emailData.put("email", this.emailId);
            final String emailDataStr = this.gson.toJson(emailData);
            wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EMAIL_KEY.value, emailDataStr).apply();
            wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.EMAIL_SYNC_KEY.value, true).apply();
            checkAndSendEmail();
        } else {

            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo initial data is not initiallized.Cannot send event information");
        }

    }

    /**
     * Method to send events to wigzo server
     */
    private synchronized void checkAndPushEvent() {

        if (!WigzoFcmListenerService.isSentToServer && StringUtils.isNotEmpty(getGeneratedToken())) {
            WigzoFcmListenerService.refreshedToken = getGeneratedToken();
            sendRegistrationToServer(WigzoFcmListenerService.refreshedToken);
        }

        boolean checkStatus = checkWigzoData();
        if (checkStatus) {
            Map<String, Object> eventData = new HashMap<>();
            WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);

            String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value, "");

            DeviceInfo deviceInfo = new DeviceInfo();
            eventData.put("deviceId", deviceId);
            eventData.put("orgToken", this.orgToken);
            eventData.put("appKey", this.appKey);
            eventData.put("deviceInfo", deviceInfo.getMetrics(this.context));

            final List<EventInfo> eventInfos = EventInfo.getEventList();
            if (!eventInfos.isEmpty()) {
                final String eventsStr = this.gson.toJson(eventInfos);
//                wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EVENTS_KEY.value, eventsStr).apply();
                eventData.put("eventData", eventsStr);
                final String eventDataStr = this.gson.toJson(eventData);
                final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                        + Configuration.EVENT_DATA_URL.value, Configuration.SITE_ID.value
                        , getOrgToken());
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        String response = ConnectionStream.postRequest(url, eventDataStr);
                        if (null != response) {
                            Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                            }.getType());
                            if ("success".equals(jsonResponse.get("status"))) {
                                EventInfo.Operation operation = EventInfo.Operation.removePartially(eventInfos);
                                EventInfo.editOperation(operation);
                                Log.e("EventInfo", "Event Sent");
                            }
                        }
                    }
                });
            }
        } else {
            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo SDK data is not initialized.Cannot send event information");
        }

    }


    private void sendFcmRead() {
        final List<FcmRead> fcmReadList = FcmRead.getFcmReadList(getContext());
        if (!fcmReadList.isEmpty()) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("data", fcmReadList);

            final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                    + Configuration.FCM_READ_URL.value, Configuration.SITE_ID.value
                    , getOrgToken());
            final String payloadStr = this.gson.toJson(payload);

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String response = ConnectionStream.postRequest(url, payloadStr);
                    if (null != response) {
                        Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                        }.getType());
                        if ("success".equals(jsonResponse.get("status"))) {
                            FcmRead.Operation operation = FcmRead.Operation.removePartially(fcmReadList);
                            FcmRead.editOperation(getContext(), operation);
                        }
                    }
                }
            });
        }
    }

    private void sendFcmOpen() {
        final List<FcmOpen> fcmOpenList = FcmOpen.getFcmOpenList(getContext());
        if (!fcmOpenList.isEmpty()) {
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("data", fcmOpenList);

            final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                    + Configuration.FCM_OPEN_URL.value, Configuration.SITE_ID.value
                    , getOrgToken());
            final String payloadStr = this.gson.toJson(payload);

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String response = ConnectionStream.postRequest(url, payloadStr);
                    if (null != response) {
                        Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                        }.getType());
                        if ("success".equals(jsonResponse.get("status"))) {
                            FcmOpen.Operation operation = FcmOpen.Operation.removePartially(fcmOpenList);
                            FcmOpen.editOperation(getContext(), operation);
                        }
                    }
                }
            });
        }
    }

    /**
     * Utility Method to prepare Map of Device Identification data. Map contains deviceId and orgToken
     *
     * @return :
     */
    public String getDeviceIdentificationData() {
        Map<String, Object> deviceData = new HashMap<>();

        //Initialise Shared Preferences
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);

        //get device id from shared preferences
        String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value, "");

        //put user details in Map to send to server
        deviceData.put("deviceId", deviceId);
        deviceData.put("orgToken", this.orgToken);
        deviceData.put("appKey", this.appKey);

        //return prepared json string from map
        return this.gson.toJson(deviceData);
    }

    /**
     * This method is used to check if WigzoSdk is initialized or not before sending any request to wigzo server
     *
     * @return
     */
    public boolean checkWigzoData() {
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value, "");
        if (StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(this.orgToken) || this.context == null) {
            return false;
        }
        return true;
    }

    /**
     * Method to track session start time. This should be called when app starts and not in all activities
     */
    public synchronized void onStart() {
        //CrashDetails.inForeground();
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        if (this.startTime == 0) {
            long currentTime = System.currentTimeMillis() / 1000l;
            wigzoSharedStorage.getSharedStorage().edit().putLong(Configuration.PREV_TIME_SPENT_KEY.value, currentTime).apply();
        } else {
            wigzoSharedStorage.getSharedStorage().edit().putLong(Configuration.PREV_TIME_SPENT_KEY.value, this.startTime).apply();

        }
        this.startTime = System.currentTimeMillis() / 1000l;

    }

    /**
     * Method to track session end time. This should be called when app is about to close and not in all activities
     */
    public synchronized void onStop() {
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(this.context);
        long prevTimeSpent = wigzoSharedStorage.getSharedStorage().getLong(Configuration.PREV_TIME_SPENT_KEY.value, 0l);
        long duration = (System.currentTimeMillis() / 1000l) - prevTimeSpent;
        duration = duration + wigzoSharedStorage.getSharedStorage().getLong(Configuration.TIME_SPENT_KEY.value, 0l);
        String durationStr = Long.toString(duration);
        if (duration >= 60) {
            wigzoSharedStorage.getSharedStorage().edit().putLong(Configuration.TIME_SPENT_KEY.value, 0l).apply();
            boolean checkStatus = checkWigzoData();
            if (checkStatus) {
                final Map<String, String> sessionData = new HashMap<>();
                String deviceId = wigzoSharedStorage.getSharedStorage().getString(Configuration.DEVICE_ID_KEY.value, "");
                String appKey = wigzoSharedStorage.getSharedStorage().getString(Configuration.APP_KEY.value, "");
                sessionData.put("deviceId", deviceId);
                sessionData.put("orgToken", this.orgToken);
                sessionData.put("appKey", appKey);
                sessionData.put("sessionData", durationStr);
                Gson gson = new Gson();
                final String sessionDataStr = gson.toJson(sessionData);
                final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                        + Configuration.SESSION_DATA_URL.value, Configuration.SITE_ID.value
                        , getOrgToken());
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        ConnectionStream.postRequest(url, sessionDataStr);
                    }
                });

            }
        } else {
            wigzoSharedStorage.getSharedStorage().edit().putLong(Configuration.TIME_SPENT_KEY.value, duration).apply();
        }
    }

    public synchronized void checkAndSendUserProfile() {
        if (checkWigzoData()) {
            final WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(getContext());
            Boolean syncData = wigzoSharedStorage.getSharedStorage().getBoolean(Configuration.USER_PROFILE_SYNC_KEY.value, false);
            if (syncData) {
                final String hasProfilePicture = wigzoSharedStorage.getSharedStorage().getString(Configuration.USER_PROFILE_PICTURE_KEY.value, "");
                final String userProfileDataStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.USER_PROFILE_DATA_KEY.value, "");
                final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                        + Configuration.USER_PROFILE_URL.value, Configuration.SITE_ID.value
                        , getOrgToken());
                if (StringUtils.isNotEmpty(userProfileDataStr)) {
                    if (StringUtils.isEmpty(hasProfilePicture)) {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                String response = ConnectionStream.postRequest(url, userProfileDataStr);
                                if (null != response) {
                                    Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                                    }.getType());
                                    if ("success".equals(jsonResponse.get("status"))) {
                                        wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.USER_PROFILE_SYNC_KEY.value, false).apply();
                                        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.USER_PROFILE_DATA_KEY.value, "").apply();
                                    } else {
                                        wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.USER_PROFILE_SYNC_KEY.value, true).apply();
                                    }
                                }
                            }
                        });
                    } else {
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                String response = ConnectionStream.postMultimediaRequest(url, userProfileDataStr, hasProfilePicture);
                                if (null != response) {
                                    Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                                    }.getType());
                                    if ("success".equals(jsonResponse.get("status"))) {
                                        wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.USER_PROFILE_SYNC_KEY.value, false).apply();
                                        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.USER_PROFILE_DATA_KEY.value, "").apply();
                                    } else {
                                        wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.USER_PROFILE_SYNC_KEY.value, true).apply();
                                    }
                                }
                            }
                        });
                    }
                } else {
                    Log.w(Configuration.WIGZO_SDK_TAG.value, "No user profile data to send");
                }
            }
        } else {
            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo SDK data is not initialized.Cannot send event information");
        }
    }

    private synchronized void checkAndSendEmail() {
        if (checkWigzoData()) {
            final WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(getContext());
            Boolean syncData = wigzoSharedStorage.getSharedStorage().getBoolean(Configuration.EMAIL_SYNC_KEY.value, false);
            final String emailData = wigzoSharedStorage.getSharedStorage().getString(Configuration.EMAIL_KEY.value, "");
            final String url = WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value
                    + Configuration.EMAIL_DATA_URL.value, Configuration.SITE_ID.value
                    , getOrgToken());
            if (syncData) {
                if (StringUtils.isNotEmpty(emailData)) {

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            String response = ConnectionStream.postRequest(url, emailData);
                            if (null != response) {
                                Map<String, Object> jsonResponse = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                                }.getType());
                                if ("success".equals(jsonResponse.get("status"))) {
                                    wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.EMAIL_SYNC_KEY.value, false).apply();
                                    wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.EMAIL_KEY.value, "").apply();
                                } else {
                                    wigzoSharedStorage.getSharedStorage().edit().putBoolean(Configuration.EMAIL_SYNC_KEY.value, true).apply();
                                }
                            }
                        }
                    });

                } else {
                    Log.w(Configuration.WIGZO_SDK_TAG.value, "No email data to send");
                }
            }
        } else {
            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo SDK data is not initialized.Cannot send event information");
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


    /**
     * This method stores the running status of the app.
     *
     * @param appRunningStatus - true, App is Running, else false
     *                         context is not required to be passed everytime, and so method overloading is done.
     */

    protected void appStatus(boolean appRunningStatus) {

        //Initialising shared preferences
        wigzoSharedStorageForAppliLifeCycle = getContext()
                .getSharedPreferences("AppStatusPref", Context.MODE_PRIVATE);

        //updated app running status in shared preferences
        wigzoSharedStorageForAppliLifeCycle.edit()
                .putBoolean(Configuration.APP_RUNNING_STATUS.value, appRunningStatus).apply();

        Log.d("State", "" + wigzoSharedStorageForAppliLifeCycle.getBoolean(Configuration.APP_RUNNING_STATUS.value, false));
    }

    /**
     * Checks the state of the app. this method is called
     * from {@link WigzoFcmListenerService} to generate notifications.
     * If the app state is running then No notification will be generated, instead, notification
     * data can be used to display In App Messages.
     */

    public boolean isAppRunning() {
        try {
            if (null == wigzoSharedStorageForAppliLifeCycle) {
                wigzoSharedStorageForAppliLifeCycle = getContext()
                        .getSharedPreferences("AppStatusPref", Context.MODE_PRIVATE);
            }
        } catch (Exception e) {
            return false;
        }

        //Fetch App Running Status from shared preferences and return corresponding bool.
        boolean appStatus = wigzoSharedStorageForAppliLifeCycle
                .getBoolean(Configuration.APP_RUNNING_STATUS.value, false);

        //Check if app is running. Return true if running
        return appStatus;
    }

    private void getDeviceLocation()
    {
        final WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context == null ? getContext() : context);

        String orgToken = wigzoSharedStorage.getSharedStorage().getString(Configuration.ORG_TOKEN_KEY.value, "");

        final String url = WigzoUrlWrapper.addQueryParam(
                WigzoUrlWrapper.addQueryParam(Configuration.BASE_URL.value + Configuration.USER_LOCATION_URL.value,  "orgId", orgToken)
                , Configuration.SITE_ID.value, orgToken);

        final Gson gson = new Gson();

        //Initialise Executor Service to get device location from server
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future future = executorService.submit(new Callable<Void>() {
            public Void call() {
                //Get response from server
                String response = ConnectionStream.getRequest(url);

                //Check if response is not null
                if (StringUtils.isNotEmpty(response)) {

                    Map<String, Object> locationMap = gson.fromJson(response, new TypeToken<HashMap<String, Object>>() {
                    }.getType());

                    String countryCode = locationMap.get("country").toString();
                    locationMap.put("countryCode", countryCode);

                    String location = gson.toJson(locationMap);

                    //Save device location
                    wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.DEVICE_LOCATION_KEY.value, location).apply();
                }
                return null;
            }
        });
    }
}
