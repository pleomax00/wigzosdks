package wigzo.sdk.model;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.helpers.Configuration;
import wigzo.sdk.helpers.ConnectionStream;

/**
 * <p>An instance of this class represents Profile of user.</p>
 * Profile information includes -
 * <ol>
 *     <li>{@link UserProfile#fullName} - Full name of user.</li>
 *     <li>{@link UserProfile#userName} - user name of user.</li>
 *     <li>{@link UserProfile#email} - email of user.</li>
 *     <li>{@link UserProfile#organization} - Organization details of application t.</li>
 *     <li>{@link UserProfile#phone} - phone number of user.</li>
 *     <li>{@link UserProfile#gender} - gender of user.</li>
 *     <li>{@link UserProfile#birthYear} - birth year of user.</li>
 *     <li>{@link UserProfile#picturePath} - user's profile picture's path.</li>
 *     <li>{@link UserProfile#customData} - Map of custom data ( any other addition data).</li>
 * </ol>
 * @author Minaz Ali
 */

public class UserProfile {

    private String fullName;
    private String userName;
    private String email;
    private String organization;
    private String phone;
    private String gender;
    private String birthYear;
    private String picturePath;
    private Map<String, String> customData;

    public UserProfile() {}

    /**
     * Constructor to obtain object of {@link UserProfile} with fullname, username , email and organization details
     * @param fullName : Full name of application user
     * @param userName :user name of application user
     * @param email : email of application user
     * @param organization : organization of application
     */
    public UserProfile(String fullName, String userName, String email, String organization) {
        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
        this.organization = organization;
    }

    /**
     * Constructor to obtain object of {@link UserProfile} with fullname, username , email ,organization details and custom data
     * @param fullName : Full name of application user
     * @param userName :user name of application user
     * @param email : email of application user
     * @param organization : organization of application
     * @param customData : this map can be used to provide any additoinal user data if any
     */
    public UserProfile(String fullName, String userName, String email, String organization, Map<String, String> customData) {
        this.fullName = fullName;
        this.organization = organization;
        this.email = email;
        this.userName = userName;
        this.customData = customData;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCustomData(Map<String, String> customData) {
        this.customData = customData;
    }


    /**
     * This method is used to save {@link UserProfile} and send the same to wigzo server
     */
    public void saveUserProfile(){
        if(WigzoSDK.getInstance().checkWigzoData()) {
            String deviceId = WigzoSDK.getInstance().getDeviceId();
            String orgToken = WigzoSDK.getInstance().getOrgToken();
            String appKey = WigzoSDK.getInstance().getAppKey();
            Gson gson = new Gson();
            Map<String, Object> userDataMap = new HashMap<>();
            userDataMap.put("deviceId", deviceId);
            userDataMap.put("orgToken", orgToken);
            userDataMap.put("appKey", appKey);
            userDataMap.put("userData", this);
            final String picturePath = this.picturePath;
            final String userDataStr = gson.toJson(userDataMap);
            final String url = Configuration.BASE_URL.value + Configuration.USER_PROFILE_URL.value;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (StringUtils.isEmpty(picturePath)) {
                        ConnectionStream.postRequest(url, userDataStr);
                    } else {
                        ConnectionStream.postMultimediaRequest(url, userDataStr, picturePath);
                    }
                }
            });
        }else{
            Log.e(Configuration.WIGZO_SDK_TAG.value, "Wigzo SDK data is not initiallized.Cannot send user information");

        }

    }
}
