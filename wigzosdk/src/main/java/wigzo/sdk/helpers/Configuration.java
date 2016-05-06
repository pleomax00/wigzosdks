package wigzo.sdk.helpers;

/**
 * Created by wigzo on 3/5/16.
 */
public enum Configuration {

    DEVICE_ID_TAG("deviceIdTag","DeviceInfo"),
    DEFAULT_SDK_VERSION("defaultSdkVersion","1.0"),
    WIGZO_SDK_TAG("wigzoSdkTag","Wigzo"),
    STORAGE_KEY("storageKey","WIGZO_SHARED_STORAGE"),
    EVENTS_KEY("eventKey","WIGZO_EVENTS"),
    DEVICE_ID_KEY("deviceIdKey","DEVICE_ID_KEY"),
    APP_KEY("appKey","APP_KEY"),
    EMAIL_KEY("emailKey","EMAIL_KEY"),
    BASE_URL("baseUrl","http://suyash.wigzoes.com"),
    INITIAL_DATA_URL("initialDataUrl","/androidsdk/getinitialdata"),
    EVENT_DATA_URL("eventDataUrl","/androidsdk/geteventdata"),
    SESSION_DATA_URL("sessionDataUrl","/androidsdk/getsessiondata"),
    USER_PROFILE_URL("userProfileUrl","/androidsdk/getuserprofiledata"),
    EMAIL_DATA_URL("emailDataUrl","/androidsdk/mapemail"),
    TIME_DELAY("timeDelay","5");

    public String key;
    public String value;

    Configuration(String key, String value)
    {
        this.key   = key;
        this.value = value;
    }


}
