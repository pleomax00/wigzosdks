package wigzo.sdk.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import wigzo.sdk.helpers.Configuration;
import wigzo.sdk.helpers.WigzoSharedStorage;

/**
 * Created by wigzo on 18/5/16.
 */
public class GcmOpen {
    public static class Operation {
        public enum OperationType {
            SAVE_ONE,
            REMOVE_PARTIALLY
        }

        OperationType operationType;
        GcmOpen gcmOpen;
        List<GcmOpen> gcmOpenList;

        public Operation() {}

        public static Operation saveOne(GcmOpen gcmOpen) {
            Operation operation = new Operation();
            operation.operationType = OperationType.SAVE_ONE;
            operation.gcmOpen = gcmOpen;
            return operation;
        }

        public static Operation removePartially(List<GcmOpen> gcmOpenList) {
            Operation operation = new Operation();
            operation.operationType = OperationType.REMOVE_PARTIALLY;
            operation.gcmOpenList = gcmOpenList;
            return operation;
        }
    }

    private long id;
    private String timestamp;

    public GcmOpen(){};

    public GcmOpen(long id, String timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized static List<GcmOpen> getGcmOpenList(Context context) {
        Gson gson = new Gson();
        List<GcmOpen> gcmOpenList = new ArrayList<>();
        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);

        String gcmOpensStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.GCM_OPEN_KEY.value, "");
        if(StringUtils.isNotEmpty(gcmOpensStr))
            gcmOpenList = gson.fromJson(gcmOpensStr, new TypeToken<List<GcmOpen>>() { }.getType());
        return gcmOpenList;
    }


    public synchronized static void editOperation(Context context, Operation operation) {
        Gson gson = new Gson();
        List<GcmOpen> gcmOpenList = new ArrayList<>();

        WigzoSharedStorage wigzoSharedStorage = new WigzoSharedStorage(context);
        String gcmOpensStr = wigzoSharedStorage.getSharedStorage().getString(Configuration.GCM_OPEN_KEY.value, "");

        if(StringUtils.isNotEmpty(gcmOpensStr)) {
            gcmOpenList = gson.fromJson(gcmOpensStr, new TypeToken<List<GcmOpen>>() { }.getType());
        }
        // operations begin
        if (operation.operationType == Operation.OperationType.SAVE_ONE) {
            gcmOpenList.add(operation.gcmOpen);
        }
        else if (operation.operationType == Operation.OperationType.REMOVE_PARTIALLY) {
            gcmOpenList.removeAll(operation.gcmOpenList);
        }
        gcmOpensStr = gson.toJson(gcmOpenList);
        wigzoSharedStorage.getSharedStorage().edit().putString(Configuration.GCM_OPEN_KEY.value, gcmOpensStr).apply();
    }
}
