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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractWigzoFcmListenerService extends FirebaseMessagingService {

    private String title = "";
    private String body = "";
    private String uuid = "";
    private Integer campaignId = 0;
    private HashMap<String, String> payload = new HashMap<>();

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
     * */
    protected abstract Class<? extends Activity> getTargetActivity();

    /**
     *
     *  Listens to the notifications arrived when app is already open
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
     *
     */
    protected abstract void notificationListener(Context context);

    /**
     * Return <B>"true"</B> if you want to display In App Messages using Wigzo SDK.
     * To Display your custom dialog return <B>"false"</B>
     * */
    protected abstract boolean showWigzoDialog();

    /**
     * returns the Key-Value pairs received via notification
     * */
    protected HashMap<String, String> getWigzoNotificationPayload() {
        return payload;
    }

    /**
     * returns the Notification Title as String
     * */
    protected String getWigzoNotificationTitle() {
        return title;
    }

    /**
     * returns the Notification Body as String
     * */
    protected String getWigzoNotificationBody() {
        return body;
    }

    public static RemoteMessage msg = null;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {

        String from = message.getFrom();

        Map data = message.getData();

        Log.e("test", "Parent Called");

        Log.d(Configuration.WIGZO_GCM_LISTENER_SERVICE_TAG.value, "From: " + from);

        String notificationIdStr = (String) data.get("notification_id");
        String intentData = (String) data.get("intent_data");
        String imageUrl = (String) data.get("image_url");
        String secondSoundStr = (String) data.get("second_sound");
        String type = (String) data.get("type");
        String campaignIdStr = (String) data.get("id");

        uuid = (String) data.get("uuid");
        body = (String) data.get("body");
        title = (String) data.get("title");

        String linkType = "TARGET_ACTIVITY";
        String link = "http://www.google.com";


        Integer secondSound = StringUtils.isEmpty(secondSoundStr) ? null : Integer.parseInt(secondSoundStr);
        Integer notificationId = StringUtils.isEmpty(notificationIdStr) ? null : Integer.parseInt(notificationIdStr);
        campaignId = StringUtils.isEmpty(notificationIdStr) ? null : Integer.parseInt(campaignIdStr);

        Log.i("msg rcvd", "Id: " + notificationId);
        Log.i("msg rcvd", "IData: " + intentData);
        Log.i("msg rcvd", "Image URL: " + imageUrl);
        Log.i("msg rcvd", "SSound: " + secondSound);
        Log.i("msg rcvd", "Body: " + body);
        Log.i("msg rcvd", "Type: " + type);
        Log.i("msg rcvd", "Title: " + title);
        Log.i("msg rcvd", "intentData: " + intentData);
        Log.i("msg rcvd", "id: " + campaignId);

        Log.e("notification", message.getData().toString());

        payload = new Gson()
                .fromJson(intentData, new TypeToken<HashMap<String, String>>() {
                }.getType());

        if (!WigzoSDK.getInstance().isAppRunning()) {

            if (StringUtils.equals(type, "simple_push")) {
                WigzoNotification.simpleNotification(getApplicationContext(), getTargetActivity(), title, body, intentData, uuid, notificationId, linkType, link, secondSound, campaignId);
            } else if (StringUtils.equals(type, "image_push")) {
                WigzoNotification.imageNotification(getApplicationContext(), getTargetActivity(), title, body, imageUrl, intentData, uuid, notificationId, linkType, link, secondSound, campaignId);
            }
        } else {
            if (!showWigzoDialog())
                notificationListener(WigzoSDK.getInstance().getContext());
            else
            {
                ((AppCompatActivity)WigzoSDK.getInstance().getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        WigzoDialogTemplate wigzoDialogTemplate
                                =new WigzoDialogTemplate(WigzoSDK.getInstance().getContext()
                                , getWigzoNotificationTitle(), getWigzoNotificationBody()
                                , getWigzoNotificationPayload());
                        wigzoDialogTemplate.show();
                    }
                });

                // increase counter for notification recieved and opened
                if (StringUtils.isNotEmpty(uuid)) {
                    final ScheduledExecutorService gcmReadWorker = Executors.newSingleThreadScheduledExecutor();
                    gcmReadWorker.schedule(new Runnable() {
                        @Override
                        public void run() {
                            GcmRead gcmRead = new GcmRead(uuid, campaignId);
                            GcmRead.Operation operationRead = GcmRead.Operation.saveOne(gcmRead);
                            GcmRead.editOperation(WigzoSDK.getInstance().getContext(), operationRead);

                            GcmOpen gcmOpen = new GcmOpen(uuid, campaignId);
                            GcmOpen.Operation operationOpen = GcmOpen.Operation.saveOne(gcmOpen);
                            GcmOpen.editOperation(WigzoSDK.getInstance().getContext(), operationOpen);
                        }
                    }, 0, TimeUnit.SECONDS);
                }
            }
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
