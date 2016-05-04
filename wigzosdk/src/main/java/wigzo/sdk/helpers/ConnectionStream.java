package wigzo.sdk.helpers;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import wigzo.sdk.WigzoSDK;
import wigzo.sdk.model.DeviceInfo;

/**
 * Created by wigzo on 28/4/16.
 */
public class ConnectionStream {

    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 30000;
    private static final int READ_TIMEOUT_IN_MILLISECONDS = 30000;

    public static boolean postRequest(String urlStr, String data)
    {
        Log.d("server url: ", urlStr);
        final URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
        }catch (Exception e) {
            e.printStackTrace();
        }

        connection.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        connection.setReadTimeout(READ_TIMEOUT_IN_MILLISECONDS);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);


        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);

            writer.flush();
            writer.close();
            final int responseCode = connection.getResponseCode();
            boolean success = responseCode >= 200 && responseCode < 300;
            if (!success && WigzoSDK.getSharedInstance().isLoggingEnabled()) {
                Log.w(Configuration.WIGZO_SDK_TAG.value, "HTTP error response code was " + responseCode + " from submitting user identification data: " + "");
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            if (connection != null && connection instanceof HttpURLConnection) {
                (connection).disconnect();
            }
        }
        return false;

    }
}
