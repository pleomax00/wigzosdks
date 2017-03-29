package com.wigzo.sdk.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wigzo.sdk.helpers.Configuration;
import com.wigzo.sdk.helpers.WigzoSharedStorage;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wigzo on 18/5/16.
 */
public class FcmOpen {
    public static class Operation {
        public enum OperationType {
            SAVE_ONE,
            REMOVE_PARTIALLY
        }

        OperationType operationType;
        FcmOpen fcmOpen;
        List<FcmOpen> fcmOpenList;

        public Operation() {}

        public static Operation saveOne(FcmOpen fcmOpen) {
            Operation operation = new Operation();
            operation.operationType = OperationType.SAVE_ONE;
            operation.fcmOpen = fcmOpen;
            return operation;
        }

        public static Operation removePartially(List<FcmOpen> fcmOpenList) {
            Operation operation = new Operation();
            operation.operationType = OperationType.REMOVE_PARTIALLY;
            operation.fcmOpenList = fcmOpenList;
            return operation;
        }
    }

    private String uuid;
    private int campaignId;
    private int organizationId;
    private String timestamp;

    public FcmOpen(String uuid, int campaignId, int organizationId) {
        this.uuid = uuid;
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getnotificationId() { return campaignId; }

    public void setNotificationId(int id) { this.campaignId = id; }

    public int getOrganizationId() { return organizationId; }

    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FcmOpen){
            FcmOpen fcmOpen = (FcmOpen) obj;
            return this.uuid.equalsIgnoreCase(fcmOpen.uuid);
        }
        return false;
    }

    public synchronized static List<FcmOpen> getFcmOpenList(Context context) {
        Gson gson = new Gson();
        List<FcmOpen> fcmOpenList = new ArrayList<>();
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);

        String fcmOpensStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.FCM_OPEN_KEY.value, "");
        if(StringUtils.isNotEmpty(fcmOpensStr))
            fcmOpenList = gson.fromJson(fcmOpensStr, new TypeToken<List<FcmOpen>>() { }.getType());
        return fcmOpenList;
    }


    public synchronized static void editOperation(Context context, Operation operation) {
        Gson gson = new Gson();
        List<FcmOpen> fcmOpenList = new ArrayList<>();

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        String fcmOpensStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.FCM_OPEN_KEY.value, "");

        if(StringUtils.isNotEmpty(fcmOpensStr)) {
            fcmOpenList = gson.fromJson(fcmOpensStr, new TypeToken<List<FcmOpen>>() { }.getType());
        }
        // operations begin
        if (operation.operationType == Operation.OperationType.SAVE_ONE) {
            fcmOpenList.add(operation.fcmOpen);
        }
        else if (operation.operationType == Operation.OperationType.REMOVE_PARTIALLY) {
            fcmOpenList.removeAll(operation.fcmOpenList);
        }
        fcmOpensStr = gson.toJson(fcmOpenList);
        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.FCM_OPEN_KEY.value, fcmOpensStr).apply();
    }
}
