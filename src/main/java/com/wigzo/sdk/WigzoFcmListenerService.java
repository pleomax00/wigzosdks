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

package com.wigzo.sdk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.helpers.Configuration;
import com.wigzo.sdk.helpers.StringUtils;
import com.wigzo.sdk.helpers.WigzoEnvironment;
import com.wigzo.sdk.helpers.WigzoFCMNotificationSupporter;
import com.wigzo.sdk.helpers.WigzoSharedStorage;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.wigzo.sdk.helpers.WigzoFCMTokenSupporter.mapFcmToDeviceId;
import static com.wigzo.sdk.helpers.WigzoFCMTokenSupporter.sendRegistrationToServer;
import static com.wigzo.sdk.helpers.WigzoFCMTokenSupporter.subscribeTopics;

@Keep
public class WigzoFcmListenerService extends FirebaseMessagingService {

    /*NOTIFICATION SPECIFIC BLOCK BEGINS*/
    private String title = "";
    private String body = "";
    private String uuid = "";
    private String type = "";
    private String pushType = "";
    private long campaignId = 0;
    private HashMap<String, String> payload = new HashMap<>();
    private Bitmap remote_picture = null;
    private int notificationId = 0;
    private String linkType = "";
    private String link = "";
    private int secondSound = 0;
    private String imageUrl = "";
    private long organizationId = 0;
    private String layoutId = "001";
    private boolean isWigzoNotification = false;
    private static Class<? extends Activity> positiveButtonClickActivity = null;

    /**
     * return the Activity which should open when notification is clicked.
     * Logic to return the activity can be based upon Notification title, body
     * or payload-key-value pairs.
     * <code><pre>
     *
     *     Example:
     *
     *     if(getWigzoNotificationPayload.get("key").equals("value"))
     *     {
     *          return MainActivity.class
     *     }
     * </pre></code>
     */
    protected Class<? extends AppCompatActivity> getTargetActivity() {
        return null;
    }

    /**
     * Listens to the notifications arrived when app is already open
     * <code><pre>
     *     ((AppCompatActivity)context).runOnUiThread(new Runnable() {
     *          {@code @Override }
     *           public void run() {
     *
     *               // write your code to show dialog for In App Messages here
     *               // use the following methods:
     *               // getWigzoNotificationPayload() : to get the notification Payload
     *               // getWigzoNotificationTitle     : to get the notification Title
     *               // getWigzoNotificationBody      : to get the notification Body
     *
     *           }
     *     });
     * </pre></code>
     */
    protected void notificationListener(Map data) {

    }

    /**
     * return the activity which should open on click of the positive button when an In App Message
     * is received when {@link WigzoFcmListenerService#showWigzoDialog()} is true. Logic to
     * return the activity can be based upon Notification title, body or payload-key-value pairs.
     * <code><pre>
     *     Example:
     *     if(getWigzoNotificationPayload.get("key").equals("value"))
     *     {
     *          return MainActivity.class
     *     }
     * </pre></code>
     * */
    protected Class<? extends AppCompatActivity> getPositiveButtonClickActivity() {
        return null;
    }

    /**
     * Return <B>"true"</B> if you want to display In App Messages using Wigzo SDK.
     * To Display your custom dialog return <B>"false"</B>
     */
    protected boolean showWigzoDialog() {
        return isWigzoNotification;
    }

    /**
     * Return <B>"true"</B> if you want to display In App Messages using Wigzo SDK.
     * To Display your custom dialog return <B>"false"</B>
     */
    protected boolean showWigzoNotifications() {
        return isWigzoNotification;
    }

    /**
     * returns the Key-Value pairs received via notification
     */
    public HashMap<String, String> getWigzoNotificationPayload() {
        return payload;
    }

    /**
     * returns the Notification Title as String
     */
    public String getWigzoNotificationTitle() {
        return title;
    }

    /**
     * returns the Notification Body as String
     */
    public String getWigzoNotificationBody() {
        return body;
    }

    /**
     * returns the bitmap if image url is present, null if no image url is present
     * */
    public Bitmap getWigzoNitificationBitmap()
    {
        return remote_picture;
    }

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {

        notificationListener(message.getData());

        JSONObject jsonObject = new JSONObject();

        Gson gson = new Gson();
        String from = message.getFrom();

        Map data = message.getData();
        isWigzoNotification = (data.keySet().contains("isWigzoNotification") ? Boolean.parseBoolean(data.get("isWigzoNotification").toString()) : false);
        if (!isWigzoNotification || !showWigzoNotifications()) return;

        imageUrl = (String) (data.keySet().contains("image_url") ? data.get("image_url") : "");
        uuid = (String) (data.keySet().contains("uuid") ? data.get("uuid") : "");
        body = (String) (data.keySet().contains("body") ? data.get("body") : "");
        title = (String) (data.keySet().contains("title") ? data.get("title") : "");
        linkType = "TARGET_ACTIVITY";
        link = "http://www.google.com";

        String notificationIdStr = (String) (data.keySet().contains("notification_id") ? data.get("notification_id") : "");
        String intentData = (String) (data.keySet().contains("intent_data") ? data.get("intent_data") : "");
        String secondSoundStr = (String) (data.keySet().contains("second_sound") ? data.get("second_sound") : "");
        String type = (String) (data.keySet().contains("type") ? data.get("type") : "");
        String campaignIdStr = (String) (data.keySet().contains("id") ? data.get("id") : "");
        String organizationIdStr = (String) (data.keySet().contains("organizationId") ? data.get("organizationId") : "");
        String layoutIdStr = (String) (data.keySet().contains("layoutId") ? data.get("layoutId") : "001");

        if (StringUtils.isEmpty(uuid, body, title, notificationIdStr, intentData, type, campaignIdStr, organizationIdStr)) {
            Log.e("INVALID JSON", "Received invalid json. Please try sending android notification from WIGZO dashboard again");
            return;
        }

        Map message_type = gson.fromJson(type, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        this.type = (String) message_type.get("type");
        this.pushType = (String) message_type.get("pushType");

        this.secondSound = StringUtils.isEmpty(secondSoundStr) ? null : Integer.parseInt(secondSoundStr);
        this.notificationId = StringUtils.isEmpty(notificationIdStr) ? null : Integer.parseInt(notificationIdStr);
        this.campaignId = StringUtils.isEmpty(campaignIdStr) ? null : Integer.parseInt(campaignIdStr);
        this.organizationId = StringUtils.isEmpty(organizationIdStr) ? null : Integer.parseInt(organizationIdStr);

        this.payload = new Gson().fromJson(intentData, new TypeToken<HashMap<String, String>>() {}.getType());

        this.layoutId = StringUtils.isEmpty(layoutIdStr) ? null : layoutIdStr;

        if(StringUtils.isNotEmpty(imageUrl)) {
            try {
                this.remote_picture = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //check if user wants to send In App Message and Push notification
        if (this.pushType.equalsIgnoreCase("both")) {

            //send push notification if app is not running
            //if app is running create In App Message
            if (!WigzoSDK.getInstance().isAppRunning()) {
                //call createNotification() method to create notification if app is not running
                WigzoFCMNotificationSupporter.createNotification(getWigzoNotificationPayload(), this.type,
                        getApplicationContext(), getTargetActivity(), title, body, uuid, notificationId,
                        linkType, link, secondSound, campaignId, organizationId, imageUrl);
            }

            //if app is running generate In App Message
            else {
                //check if users want to show our builtin dialog of want to show their own custom dialog
                //if they want to show their own custom dialog then trigger notificationlistener() method
                //if users want to show our dialog then trigger generateInAppMessage() method
                if (showWigzoDialog()) {
                    WigzoFCMNotificationSupporter.generateInAppMessage(imageUrl, getWigzoNotificationTitle(),
                            getWigzoNotificationBody(), getWigzoNotificationPayload(), getWigzoNitificationBitmap(),
                            getPositiveButtonClickActivity(), layoutId);
                }

                // increase counter for notification recieved and opened for In App Message
                WigzoFCMNotificationSupporter.increaseNotificationReceivedOpenedCounter(uuid, campaignId, organizationId);
            }
        }

        else if (this.pushType.equalsIgnoreCase("push") && !WigzoSDK.getInstance().isAppRunning())
        {
            //call createNotification() method to create notification if app is not running
            WigzoFCMNotificationSupporter.createNotification(getWigzoNotificationPayload(), this.type,
                    getApplicationContext(), getTargetActivity(), title, body, uuid, notificationId,
                    linkType, link, secondSound, campaignId, organizationId, imageUrl);
        }

        else if (this.pushType.equalsIgnoreCase("inapp") && WigzoSDK.getInstance().isAppRunning())
        {
            //generarte In APp Message
            WigzoFCMNotificationSupporter.generateInAppMessage(imageUrl, getWigzoNotificationTitle(),
                    getWigzoNotificationBody(), getWigzoNotificationPayload(), getWigzoNitificationBitmap(),
                    getPositiveButtonClickActivity(), layoutId);
            WigzoFCMNotificationSupporter.increaseNotificationReceivedOpenedCounter(uuid, campaignId, organizationId);
        }

        else {
            //do nothing
        }

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }
    }

    /*NOTIFICATION SPECIFIC BLOCK ENDS*/

    /*FCM ID SPECIFIC BLOCK BEGINS*/
    /*private static final String TAG = "MyInstanceIDLS";*/
    public static String refreshedToken;
    public static boolean isSentToServer = false;
    public static String fcmSenderId = "";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]

    @Override
    public void onNewToken(String token) {

        // Get updated InstanceID token.
        fcmSenderId = WigzoEnvironment.getFCMSenderId();
        refreshedToken = token;
        Log.d("token", refreshedToken);
        if (null == WigzoSDK.getInstance().getContext()) return;

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(WigzoSDK.getInstance().getContext());
        if (null == wigzoSharedStorage) return;

        SharedPreferences sharedPreferences = wigzoSharedStorage.getSharedStorage();
        if (null == sharedPreferences) return;

        // Setting SENT_FCM_TOKEN_TO_SERVER to false since we want to refresh the token.
        sharedPreferences.edit().putBoolean(Configuration.SENT_FCM_TOKEN_TO_SERVER.value, false).apply();
        sharedPreferences.edit().putBoolean(Configuration.FCM_DEVICE_MAPPED.value, false).apply();

        sendRegistrationToServer(refreshedToken);
        if (StringUtils.isNotEmpty(wigzoSharedStorage.getSharedStorage().getString(Configuration.ORG_TOKEN_KEY.value, "")) ||
                StringUtils.isNotEmpty(WigzoSDK.getInstance().getOrgToken())) {
            mapFcmToDeviceId(refreshedToken);
        }

        try {
            subscribeTopics(WigzoSDK.getInstance().getOrgToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // [END refresh_token]
}
