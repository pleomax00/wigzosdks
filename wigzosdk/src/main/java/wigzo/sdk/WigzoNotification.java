package wigzo.sdk;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Map;
import java.util.Random;

/**
 * Created by ankit on 16/5/16.
 */
public class WigzoNotification {
    public static void simpleNotification(Context applicationContext, Class<? extends Activity> targetActivity, String title, String body, Map<String, Object> intentDataMap, Integer notification_id) {
        // if notification_id is provided use it.
        int mNotificationId = null != notification_id ? notification_id : new Random().nextInt();
        int icon = applicationContext.getApplicationInfo().icon;


        Intent resultIntent = new Intent(applicationContext, targetActivity);

        if (null != intentDataMap) {
            for (Map.Entry<String, Object> entry : intentDataMap.entrySet())
            {
                if (entry.getValue() instanceof CharSequence) {
                    resultIntent.putExtra(entry.getKey(), (CharSequence) entry.getValue());
                }
                else if (entry.getValue() instanceof Number) {
                    resultIntent.putExtra(entry.getKey(), (Number) entry.getValue());
                }
                else if (entry.getValue() instanceof Boolean) {
                    resultIntent.putExtra(entry.getKey(), (Boolean) entry.getValue());
                }
            }
        }

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        applicationContext,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(applicationContext)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);


        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) applicationContext.getSystemService(applicationContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notificationBuilder.build());
    }
}
