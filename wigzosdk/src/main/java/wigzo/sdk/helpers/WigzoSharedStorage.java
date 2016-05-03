package wigzo.sdk.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import wigzo.sdk.model.EventInfo;

/**
 * Created by wigzo on 28/4/16.
 */

/**
 * This class provides a persistence layer for the local event &amp; connection queues.
 */

public class WigzoSharedStorage {

    public  SharedPreferences getSharedStorage() {
        return sharedStorage;
    }

    private static SharedPreferences sharedStorage = null;

    /**
     * Constructs a WigzoStore object.
     * @param context used to retrieve storage meta data, must not be null.
     * @throws IllegalArgumentException if context is null
     */
    public WigzoSharedStorage(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("must provide valid context");
        }
        sharedStorage = context.getSharedPreferences(Configuration.STORAGE_KEY.value, Context.MODE_PRIVATE);
    }


    public List<EventInfo> getEventList(){
        List<EventInfo> eventInfoList = new ArrayList<>();
        Gson gson = new Gson();
        String eventsStr = sharedStorage.getString(Configuration.EVENTS_KEY.value, "");
        if(eventsStr != null && !eventsStr.isEmpty())
            eventInfoList = gson.fromJson(eventsStr, new TypeToken<List<EventInfo>>() { }.getType());
        return eventInfoList;
    }

    /**
     * Retrieves a preference from local store.
     * @param key the preference key
     */
    public synchronized String getPreference(final String key) {
        return sharedStorage.getString(key, null);
    }
}
