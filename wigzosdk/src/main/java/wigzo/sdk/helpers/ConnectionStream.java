package wigzo.sdk.helpers;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import wigzo.sdk.WigzoSDK;

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
            if (!success && WigzoSDK.getInstance().isLoggingEnabled()) {
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

    public static boolean postMultimediaRequest(String urlStr, String data, String picturePath)
    {
        Log.d("server url: ", urlStr);
        urlStr = urlStr +"?userdata="+data;
        URL url;
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
            connection.setDoOutput(true);
            File binaryFile = new File(picturePath);
                // Just generate some unique random value.
            String boundary = Long.toHexString(System.currentTimeMillis());
                // Line separator required by multipart/form-data.
            String CRLF = "\r\n";
            String charset = "UTF-8";
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
                // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            FileInputStream fileInputStream = new FileInputStream(binaryFile);
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = fileInputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                }
            } catch (IOException ex) {
                    ex.printStackTrace();
            }
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
            fileInputStream.close();
            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
            writer.flush();
            writer.close();

            final int responseCode = connection.getResponseCode();
            boolean success = responseCode >= 200 && responseCode < 300;
            if (!success && WigzoSDK.getInstance().isLoggingEnabled()) {
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

    /**
     *
     File binaryFile = new File(picturePath);
     conn.setDoOutput(true);
     // Just generate some unique random value.
     String boundary = Long.toHexString(System.currentTimeMillis());
     // Line separator required by multipart/form-data.
     String CRLF = "\r\n";
     String charset = "UTF-8";
     conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
     OutputStream output = conn.getOutputStream();
     PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
     // Send binary file.
     writer.append("--" + boundary).append(CRLF);
     writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
     writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
     writer.append("Content-Transfer-Encoding: binary").append(CRLF);
     writer.append(CRLF).flush();
     FileInputStream fileInputStream = new FileInputStream(binaryFile);
     byte[] buffer = new byte[1024];
     int len;
     try {
     while ((len = fileInputStream.read(buffer)) != -1) {
     output.write(buffer, 0, len);
     }
     }catch(IOException ex){
     ex.printStackTrace();
     }
     output.flush(); // Important before continuing with writer!
     writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
     fileInputStream.close();
     */
}
