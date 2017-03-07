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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.helpers.Configuration;
import com.wigzo.sdk.model.GcmOpen;
import com.wigzo.sdk.model.GcmRead;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractWigzoFcmListenerService extends FirebaseMessagingService {

    private String title = "";
    private String body = "";
    private String uuid = "";
    private String type = "";
    private String pushType = "";
    private Integer campaignId = 0;
    private HashMap<String, String> payload = new HashMap<>();
    private Bitmap remote_picture = null;
    private Integer notificationId = 0;
    private String linkType = "";
    private String link = "";
    private Integer secondSound = 0;
    private String imageUrl = "";
    private Integer organizationId = 0;
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
    protected abstract Class<? extends Activity> getTargetActivity();

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
    protected abstract void notificationListener(Context context);

    /**
     * return the activity which should open on click of the positive button when an In App Message
     * is received when {@link AbstractWigzoFcmListenerService#showWigzoDialog()} is true. Logic to
     * return the activity can be based upon Notification title, body or payload-key-value pairs.
     * <code><pre>
     *     Example:
     *     if(getWigzoNotificationPayload.get("key").equals("value"))
     *     {
     *          return MainActivity.class
     *     }
     * </pre></code>
     * */
    public abstract Class<? extends AppCompatActivity> getPositiveButtonClickActivity();

    /**
     * Return <B>"true"</B> if you want to display In App Messages using Wigzo SDK.
     * To Display your custom dialog return <B>"false"</B>
     */
    protected abstract boolean showWigzoDialog();

    /**
     * returns the Key-Value pairs received via notification
     */
    protected HashMap<String, String> getWigzoNotificationPayload() {
        return payload;
    }

    /**
     * returns the Notification Title as String
     */
    protected String getWigzoNotificationTitle() {
        return title;
    }

    /**
     * returns the Notification Body as String
     */
    protected String getWigzoNotificationBody() {
        return body;
    }

    /**
     * returns the bitmap if image url is present, null if no image url is present
     * */
    protected Bitmap getWigzoNitificationBitmap()
    {
        return remote_picture;
    }

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {

        String from = message.getFrom();

        Map data = message.getData();

        Log.e("test", "Parent Called");

        Log.d(Configuration.WIGZO_GCM_LISTENER_SERVICE_TAG.value, "From: " + from);

        String notificationIdStr = (String) data.get("notification_id");
        String intentData = (String) data.get("intent_data");
        imageUrl = (String) data.get("image_url");
        String secondSoundStr = (String) data.get("second_sound");
        String type = (String) data.get("type");
        String campaignIdStr = (String) data.get("id");
        String organizationIdStr = (String) data.get("organizationId");

        uuid = (String) data.get("uuid");
        body = (String) data.get("body");
        title = (String) data.get("title");

        linkType = "TARGET_ACTIVITY";
        link = "http://www.google.com";

        Gson gson = new Gson();

        Map message_type = gson.fromJson(type, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        this.type = (String) message_type.get("type");
        this.pushType = (String) message_type.get("pushType");

        this.secondSound = StringUtils.isEmpty(secondSoundStr) ? null : Integer.parseInt(secondSoundStr);
        this.notificationId = StringUtils.isEmpty(notificationIdStr) ? null : Integer.parseInt(notificationIdStr);
        this.campaignId = StringUtils.isEmpty(campaignIdStr) ? null : Integer.parseInt(campaignIdStr);
        this.organizationId = StringUtils.isEmpty(organizationIdStr) ? null : Integer.parseInt(organizationIdStr);

        Log.d("msg rcvd", "Id: " + notificationId);
        Log.d("msg rcvd", "IData: " + intentData);
        Log.d("msg rcvd", "Image URL: " + imageUrl);
        Log.d("msg rcvd", "SSound: " + secondSound);
        Log.d("msg rcvd", "Body: " + body);
        Log.d("msg rcvd", "Type: " + type);
        Log.d("msg rcvd", "Title: " + title);
        Log.d("msg rcvd", "intentData: " + intentData);
        Log.d("msg rcvd", "id: " + campaignId);
        Log.d("msg rcvd", "organizationId: " + organizationId);

        Log.e("notification", message.getData().toString());

        this.payload = new Gson().fromJson(intentData, new TypeToken<HashMap<String, String>>() {}.getType());

        if(StringUtils.isNotEmpty(imageUrl)) {
            try {
                this.remote_picture = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //check if user wants to send In App Message and Push notification
        if (StringUtils.equalsIgnoreCase("both", this.pushType)) {

            //send push notification if app is not running
            //if app is running create In App Message
            if (!WigzoSDK.getInstance().isAppRunning()) {
                //call createNotification() method to create notification if app is not running
                createNotification();
            }

            //if app is running generate In App Message
            else {
                //check if users want to show our builtin dialog of want to show their own custom dialog
                //if they want to show their own custom dialog then trigger notificationlistener() method
                if (!showWigzoDialog())
                    notificationListener(WigzoSDK.getInstance().getContext());
                //if users want to show our dialog then trigger generateInAppMessage() method
                else {
                    generateInAppMessage();
                }

                // increase counter for notification recieved and opened for In App Message
                increaseNotificationReceivedOpenedCounter();
            }
        }

        else if (StringUtils.equalsIgnoreCase("push", this.pushType))
        {
            //call createNotification() method to create notification if app is not running
            createNotification();
        }

        else if (StringUtils.equalsIgnoreCase("inapp", this.pushType))
        {
            //generarte In APp Message
            generateInAppMessage();
            increaseNotificationReceivedOpenedCounter();
        }

        else {
            //do nothing
        }

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
//        sendNotification(message);
        // [END_EXCLUDE]
    }

    private void createNotification() {
        if (StringUtils.equalsIgnoreCase(this.type, "simple")) {
            WigzoNotification.simpleNotification(getApplicationContext(), getTargetActivity(), title, body, getWigzoNotificationPayload().toString(), uuid, notificationId, linkType, link, secondSound, campaignId, organizationId);
        } else if (StringUtils.equalsIgnoreCase(this.type, "image")) {
            WigzoNotification.imageNotification(getApplicationContext(), getTargetActivity(), title, body, imageUrl, getWigzoNotificationPayload().toString(), uuid, notificationId, linkType, link, secondSound, campaignId, organizationId);
        }

    }

    private void generateInAppMessage() {
        ((AppCompatActivity) WigzoSDK.getInstance().getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(StringUtils.isNotEmpty(imageUrl)) {
                    WigzoDialogTemplate wigzoDialogTemplate
                            = new WigzoDialogTemplate(WigzoSDK.getInstance().getContext()
                            , getWigzoNotificationTitle(), getWigzoNotificationBody()
                            , getWigzoNotificationPayload(), remote_picture, getPositiveButtonClickActivity());
                    wigzoDialogTemplate.show();
                }
                else {
                    WigzoDialogTemplate wigzoDialogTemplate
                            = new WigzoDialogTemplate(WigzoSDK.getInstance().getContext()
                            , getWigzoNotificationTitle(), getWigzoNotificationBody()
                            , getWigzoNotificationPayload(), getPositiveButtonClickActivity());
                    wigzoDialogTemplate.show();
                }
            }
        });
    }

    private void increaseNotificationReceivedOpenedCounter()
    {
        if (StringUtils.isNotEmpty(uuid)) {
            final ScheduledExecutorService gcmReadWorker = Executors.newSingleThreadScheduledExecutor();
            gcmReadWorker.schedule(new Runnable() {
                @Override
                public void run() {
                    GcmRead gcmRead = new GcmRead(uuid, campaignId, organizationId);
                    GcmRead.Operation operationRead = GcmRead.Operation.saveOne(gcmRead);
                    GcmRead.editOperation(WigzoSDK.getInstance().getContext(), operationRead);

                    GcmOpen gcmOpen = new GcmOpen(uuid, campaignId, organizationId);
                    GcmOpen.Operation operationOpen = GcmOpen.Operation.saveOne(gcmOpen);
                    GcmOpen.editOperation(WigzoSDK.getInstance().getContext(), operationOpen);
                }
            }, 0, TimeUnit.SECONDS);
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    /*private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());
    }*/
}
