package com.wigzo.sdk.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.wigzo.sdk.WigzoDialogTemplate;
import com.wigzo.sdk.WigzoNotification;
import com.wigzo.sdk.WigzoSDK;
import com.wigzo.sdk.model.FcmOpen;
import com.wigzo.sdk.model.FcmRead;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WigzoFCMNotificationSupporter {
    public static void createNotification(HashMap<String, String> wigzoNotificationPayload, String type,
                                          Context context, Class<? extends AppCompatActivity> targetActivity, String title,
                                          String body, String uuid, int notificationId, String linkType,
                                          String link, int secondSound, long campaignId, long organizationId,
                                          String imageUrl) {

        Gson gson = new Gson();
        String payloadJsonStr = gson.toJson(wigzoNotificationPayload);

        if (type.equalsIgnoreCase("simple")) {
            WigzoNotification.simpleNotification(context, targetActivity, title, body, payloadJsonStr,
                    uuid, notificationId, linkType, link, secondSound, campaignId, organizationId);
        } else if (type.equalsIgnoreCase("image")) {
            WigzoNotification.imageNotification(context, targetActivity, title,
                    body, imageUrl, payloadJsonStr, uuid, notificationId,
                    linkType, link, secondSound, campaignId, organizationId);
        }

    }

    public static void generateInAppMessage(final String imageUrl, final String title, final String body, final HashMap<String,
            String> payload, final Bitmap remotePicture, final Class<? extends AppCompatActivity> positiveClickActivity,
                                            final String layoutId) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(StringUtils.isNotEmpty(imageUrl)) {
                    WigzoDialogTemplate wigzoDialogTemplate
                            = new WigzoDialogTemplate(WigzoSDK.getInstance().getContext(),
                            title,
                            body,
                            payload,
                            remotePicture,
                            positiveClickActivity,
                            layoutId);
                    wigzoDialogTemplate.show();
                }
                else {
                    WigzoDialogTemplate wigzoDialogTemplate
                            = new WigzoDialogTemplate(WigzoSDK.getInstance().getContext(),
                            title,
                            body,
                            payload,
                            positiveClickActivity);
                    wigzoDialogTemplate.show();
                }
            }
        });
    }

    public static void increaseNotificationReceivedOpenedCounter(final String uuid, final long campaignId,
                                                                 final long organizationId)
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
}
