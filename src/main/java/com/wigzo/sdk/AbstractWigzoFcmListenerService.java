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
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.helpers.StringUtils;
import com.wigzo.sdk.model.FcmOpen;
import com.wigzo.sdk.model.FcmRead;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Keep
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
    protected abstract Class<? extends AppCompatActivity> getPositiveButtonClickActivity();

    /**
     * Return <B>"true"</B> if you want to display In App Messages using Wigzo SDK.
     * To Display your custom dialog return <B>"false"</B>
     */
    protected abstract boolean showWigzoDialog();

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

        Gson gson = new Gson();
        String from = message.getFrom();

        Map data = message.getData();

        imageUrl = (String) data.get("image_url");
        uuid = (String) data.get("uuid");
        body = (String) data.get("body");
        title = (String) data.get("title");

        linkType = "TARGET_ACTIVITY";
        link = "http://www.google.com";

        String notificationIdStr = (String) data.get("notification_id");
        String intentData = (String) data.get("intent_data");
        String secondSoundStr = (String) data.get("second_sound");
        String type = (String) data.get("type");
        String campaignIdStr = (String) data.get("id");
        String organizationIdStr = (String) data.get("organizationId");

        Map message_type = gson.fromJson(type, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        this.type = (String) message_type.get("type");
        this.pushType = (String) message_type.get("pushType");

        this.secondSound = StringUtils.isEmpty(secondSoundStr) ? null : Integer.parseInt(secondSoundStr);
        this.notificationId = StringUtils.isEmpty(notificationIdStr) ? null : Integer.parseInt(notificationIdStr);
        this.campaignId = StringUtils.isEmpty(campaignIdStr) ? null : Integer.parseInt(campaignIdStr);
        this.organizationId = StringUtils.isEmpty(organizationIdStr) ? null : Integer.parseInt(organizationIdStr);

        this.payload = new Gson().fromJson(intentData, new TypeToken<HashMap<String, String>>() {}.getType());

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

        else if (this.pushType.equalsIgnoreCase("push") && !WigzoSDK.getInstance().isAppRunning())
        {
            //call createNotification() method to create notification if app is not running
            createNotification();
        }

        else if (this.pushType.equalsIgnoreCase("inapp") && WigzoSDK.getInstance().isAppRunning())
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
    }

    private void createNotification() {
        if (this.type.equalsIgnoreCase("simple")) {
            WigzoNotification.simpleNotification(getApplicationContext(), getTargetActivity(), title, body, getWigzoNotificationPayload().toString(), uuid, notificationId, linkType, link, secondSound, campaignId, organizationId);
        } else if (this.type.equalsIgnoreCase("image")) {
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
            final ScheduledExecutorService fcmReadWorker = Executors.newSingleThreadScheduledExecutor();
            fcmReadWorker.schedule(new Runnable() {
                @Override
                public void run() {
                    FcmRead fcmRead = new FcmRead(uuid, campaignId, organizationId);
                    FcmRead.Operation operationRead = FcmRead.Operation.saveOne(fcmRead);
                    FcmRead.editOperation(WigzoSDK.getInstance().getContext(), operationRead);

                    FcmOpen fcmOpen = new FcmOpen(uuid, campaignId, organizationId);
                    FcmOpen.Operation operationOpen = FcmOpen.Operation.saveOne(fcmOpen);
                    FcmOpen.editOperation(WigzoSDK.getInstance().getContext(), operationOpen);
                }
            }, 0, TimeUnit.SECONDS);
        }
    }
    // [END receive_message]
}
