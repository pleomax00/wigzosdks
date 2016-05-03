package wigzo.sdk.helpers;

/**
 * Created by wigzo on 3/5/16.
 */
public enum Configuration {

    DEVICE_ID_TAG("deviceIdTag","DeviceInfo"),
    DEFAULT_SDK_VERSION("defaultSdkVersion","1.0"),
    WIGZO_SDK_TAG("wigzoSdkTag","Wigzo"),
    EVENT_QUEUE_SIZE_THRESHOLD("eventQSizeThreshold","1"),
    STORAGE_KEY("storageKey","WIGZO_SHARED_STORAGE"),
    EVENTS_KEY("eventKey","WIGZO_EVENTS"),
    DEVICE_ID_KEY("deviceIdKey","DEVICE_ID_KEY");
    /*
      private static final String LOCATION_KEY = "LOCATION";
     */
    public String key;
    public String value;

    Configuration(String key, String value)
    {
        this.key   = key;
        this.value = value;
    }


}
