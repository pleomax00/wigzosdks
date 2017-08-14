package com.wigzo.sdk.helpers;

/**
 * Created by wigzo on 14/8/17.
 */

public class WigzoUrlWrapper {
    public static String addQueryParam(String url, String queryParamKey, String queryParamVal) {

        if (StringUtils.isNotEmpty(url) && !url.contains(queryParamKey)) {
            if (url.contains("?")) {
                url = url + "&" + queryParamKey + "=" + queryParamVal;
            } else {
                url = url + "?" + queryParamKey + "=" + queryParamVal;
            }
            return url;
        }
        return null;
    }
}
