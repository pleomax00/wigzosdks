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
public class FcmRead {
    public static class Operation {
        public enum OperationType {
            SAVE_ONE,
            REMOVE_PARTIALLY
        }

        OperationType operationType;
        FcmRead fcmRead;
        List<FcmRead> fcmReadList;

        public Operation() {}

        public static Operation saveOne(FcmRead fcmRead) {
            Operation operation = new Operation();
            operation.operationType = OperationType.SAVE_ONE;
            operation.fcmRead = fcmRead;
            return operation;
        }

        public static Operation removePartially(List<FcmRead> fcmReadList) {
            Operation operation = new Operation();
            operation.operationType = OperationType.REMOVE_PARTIALLY;
            operation.fcmReadList = fcmReadList;
            return operation;
        }
    }

    private String uuid;
    private String timestamp;
    private int campaignId;
    private int organizationId;

    public FcmRead(){};

    public FcmRead(String uuid, int campaignId, int organizationId) {
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
        if(obj instanceof FcmRead){
            FcmRead fcmRead = (FcmRead) obj;
            return this.uuid.equalsIgnoreCase(fcmRead.uuid);
        }
        return false;
    }

    public synchronized static List<FcmRead> getFcmReadList(Context context) {
        Gson gson = new Gson();
        List<FcmRead> fcmReadList = new ArrayList<>();
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);

        String fcmReadsStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.FCM_READ_KEY.value, "");
        if(StringUtils.isNotEmpty(fcmReadsStr))
            fcmReadList = gson.fromJson(fcmReadsStr, new TypeToken<List<FcmRead>>() { }.getType());
        return fcmReadList;
    }


    public synchronized static void editOperation(Context context, Operation operation) {
        Gson gson = new Gson();
        List<FcmRead> fcmReadList = new ArrayList<>();

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        String fcmReadsStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.FCM_READ_KEY.value, "");

        if(StringUtils.isNotEmpty(fcmReadsStr)) {
            fcmReadList = gson.fromJson(fcmReadsStr, new TypeToken<List<FcmRead>>() { }.getType());
        }
        // operations begin
        if (operation.operationType == Operation.OperationType.SAVE_ONE) {
            fcmReadList.add(operation.fcmRead);
        }
        else if (operation.operationType == Operation.OperationType.REMOVE_PARTIALLY) {
            fcmReadList.removeAll(operation.fcmReadList);
        }
        fcmReadsStr = gson.toJson(fcmReadList);
        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.FCM_READ_KEY.value, fcmReadsStr).apply();
    }
}
