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
 * Created by wigzo on 6/5/16.
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

    public UserProfile(String fullName, String userName, String email, String organization) {
        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
        this.organization = organization;
    }

    public UserProfile(String fullName, String organization, String email, String userName, Map<String, String> customData) {
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

    public void save(){
        if(WigzoSDK.getSharedInstance().checkWigzoData()) {
            String deviceId = WigzoSDK.getSharedInstance().getDeviceId();
            String orgToken = WigzoSDK.getSharedInstance().getOrgToken();
            String appKey = WigzoSDK.getSharedInstance().getAppKey();
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
