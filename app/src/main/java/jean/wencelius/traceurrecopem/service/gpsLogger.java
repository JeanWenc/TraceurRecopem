package jean.wencelius.traceurrecopem.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.MapAndTrackActivity;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;

public class gpsLogger extends Service implements LocationListener {

    public int pointCount;

    /**Data helper.*/
    private DataHelper dataHelper;

    /** LocationManager*/
    private LocationManager lmgr;

    /** Are we currently tracking ?*/
    private boolean isTracking = false;
    /**Is GPS enabled ?*/
    private boolean isGpsEnabled = false;

    /**Current Track ID*/
    private long currentTrackId = -1;

    /**the interval (in ms) to log GPS fixes defined in the preferences*/
    private long gpsLoggingInterval;
    private long gpsLoggingMinDistance;

    /**Last known location*/
    private Location lastLocation;

    /**the timestamp of the last GPS fix we used*/
    private long lastGPSTimestamp = 0;

    /**System notification id.*/
    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "recopemTraceur_Channel";


    public gpsLogger() {
    }

    /**Binder for service interaction*/
    private final IBinder binder = new gpsLoggerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // If we aren't currently tracking we can stop ourselves
        if (! isTracking ) {
            stopSelf();
        }
        // We don't want onRebind() to be called, so return false.
        return false;
    }

    /**Bind interface for service interaction*/
    public class gpsLoggerBinder extends Binder {
        /**
         * Called by the activity when binding.Returns itself.
         * @return the gpsLogger service
         */
        public gpsLogger getService() {
            return gpsLogger.this;
        }
    }

    /**Getter for gpsEnabled
     * @return true if GPS is enabled, otherwise false.*/
    public boolean isGpsEnabled() {
        return isGpsEnabled;
    }

    /**Getter for isTracking
     * @return true if we're currently tracking, otherwise false.*/
    public boolean isTracking() {
        return isTracking;
    }

    /**Receives Intent for way point tracking, and stop/start logging.*/
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (recopemValues.INTENT_TRACK_WP.equals(intent.getAction())) {
                // Track a way point
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    // because of the gps logging interval our last fix could be very old
                    // so we'll request the last known location from the gps provider
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        lastLocation = lmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            Long trackId = extras.getLong(TrackContentProvider.Schema.COL_TRACK_ID);
                            String uuid = extras.getString(recopemValues.INTENT_KEY_UUID);
                            String name = extras.getString(recopemValues.INTENT_KEY_UUID);

                            dataHelper.wayPoint(trackId, lastLocation, name, uuid);
                        }
                    }
                }
            } else
            if (recopemValues.INTENT_START_TRACKING.equals(intent.getAction()) ) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    long trackId = extras.getLong(TrackContentProvider.Schema.COL_TRACK_ID);
                    startTracking(trackId);
                }
            } else if (recopemValues.INTENT_STOP_TRACKING.equals(intent.getAction()) ) {
                stopTrackingAndSave();
            }
        }
    };

    @Override
    public void onCreate() {
        dataHelper = new DataHelper(this);

        pointCount=0;

        gpsLoggingInterval = 5000;
        gpsLoggingMinDistance=5;

        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(recopemValues.INTENT_TRACK_WP);
        filter.addAction(recopemValues.INTENT_START_TRACKING);
        filter.addAction(recopemValues.INTENT_STOP_TRACKING);
        registerReceiver(receiver, filter);

        // Register ourselves for location updates
        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, gpsLoggingMinDistance, this);
        }

        super.onCreate();
    }

    @Override
    public void onLocationChanged(Location location) {
        // We're receiving location, so GPS is enabled
        isGpsEnabled = true;

        // first of all we check if the time from the last used fix to the current fix is greater than the logging interval
        //if((lastGPSTimestamp + gpsLoggingInterval) < System.currentTimeMillis()){
            lastGPSTimestamp = System.currentTimeMillis(); // save the time of this fix

            lastLocation = location;

            if (isTracking) {
                dataHelper.track(currentTrackId, location);
                pointCount++;
            }
        //}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (isTracking) {
            // If we're currently tracking, save user data.
            stopTrackingAndSave();
        }
        // Unregister listener
        lmgr.removeUpdates(this);
        // Unregister broadcast receiver
        unregisterReceiver(receiver);
        // Cancel any existing notification
        stopNotifyBackgroundService();
        super.onDestroy();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not interested in provider status
    }

    @Override
    public void onProviderEnabled(String provider) {
        isGpsEnabled = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        isGpsEnabled = false;
    }

    /**
     * Start GPS tracking.
     */
    private void startTracking(long trackId) {
        currentTrackId = trackId;

        // Refresh notification with correct Track ID
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.notify(NOTIFICATION_ID, getNotification());
        isTracking = true;
    }

    /**
     * Stops GPS Logging
     */
    private void stopTrackingAndSave() {
        isTracking = false;
        dataHelper.stopTracking(currentTrackId);
        this.stopSelf();
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name_w_space);
            String description = "Display when tracking in Background";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**Builds the notification to display when tracking in background.*/
    private Notification getNotification() {

        Intent startDisplayMapActivity = new Intent(this, MapAndTrackActivity.class);
        startDisplayMapActivity.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startDisplayMapActivity, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_recopem)
                .setContentTitle(getResources().getString(R.string.tracking_notification_title) +currentTrackId)
                .setContentText(getResources().getString(R.string.tracking_notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        return mBuilder.build();
    }

    private void stopNotifyBackgroundService() {
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.cancel(NOTIFICATION_ID);
    }

    public int getPointCount() {
        return this.pointCount;
    }

}