package com.wigzo.sdk.helpers;

import android.support.annotation.Keep;

/**
 * Created by wigzo on 16/3/17.
 */
@Keep
public class StringUtils {
    public static boolean isEmpty(String string) {
        if (null == string) {
            return true;
        } else {
            string = string.trim();
            return string.equals("");
        }
    }

    public static boolean isEmpty(String ... string) {
        if (null == string) {
            return true;
        } else {
            for (String str : string) {
                if (isEmpty(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }
}
