package jean.wencelius.traceurrecopem.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.MenuActivity;
import jean.wencelius.traceurrecopem.recopemValues;

/**
 * Created by Jean Wencélius on 25/05/2020.
 */
public class Notification_receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent MenuActivityIntent = new Intent(context, MenuActivity.class);
        MenuActivityIntent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, recopemValues.REQUEST_CODE_DAILY_NOTIFICATION, MenuActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_recopem)
                .setContentTitle(context.getResources().getString(R.string.main_notification_title))
                .setContentText(context.getResources().getString(R.string.main_notification_message))
                .setAutoCancel(true);

        notificationManager.notify(100,builder.build());
    }
}
