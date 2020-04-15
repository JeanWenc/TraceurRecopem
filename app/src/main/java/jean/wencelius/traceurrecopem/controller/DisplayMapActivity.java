package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.service.gpsLogger;
import jean.wencelius.traceurrecopem.service.gpsLoggerServiceConnection;
import jean.wencelius.traceurrecopem.recopemValues;

public class DisplayMapActivity extends AppCompatActivity {

    /**
     * TODO: Make sure that when on this activity cannot go back. Or Make sure that app remembers that is_tracking
     */

    private static final String STATE_IS_TRACKING = "isTracking";

    /**GPS Logger service, to receive events and be able to update UI.*/
    private gpsLogger mGpsLogger;

    /**GPS Logger service intent, to be used in start/stopService();*/
    private Intent mGpsLoggerServiceIntent;

    /**Flag to check GPS status at startup.*/
    private boolean checkGPSFlag = true;

    /** Handles the bind to the GPS Logger service*/
    private ServiceConnection gpsLoggerConnection = new gpsLoggerServiceConnection(this);

    private Boolean IS_BEACON_SHOWING;

    public long currentTrackId;

    public TextView mShowTrackId;
    public TextView mShowPointCount;
    private ImageButton btCenterMap;
    private ImageButton btRecordTrack;
    private ImageButton btShowBeacon;
    public ImageButton btMyTracks;
    MapView mMap = null;

    private MyLocationNewOverlay mLocationOverlay;

    private Bitmap mPersonIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_map);

        currentTrackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

        mShowTrackId = (TextView) findViewById(R.id.activity_display_map_show_track_id);
        mShowPointCount = (TextView) findViewById(R.id.activity_display_map_show_point_count);

        mMap = (MapView) findViewById(R.id.activity_display_map_map);
        btCenterMap = (ImageButton) findViewById(R.id.activity_display_map_ic_center_map);
        btRecordTrack = (ImageButton) findViewById(R.id.activity_display_map_ic_record_track);
        btMyTracks = (ImageButton) findViewById(R.id.activity_display_map_ic_my_tracks);
        btShowBeacon = (ImageButton) findViewById(R.id.activity_display_map_ic_show_beacon);

        btMyTracks.setEnabled(false);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        IS_BEACON_SHOWING=false;

        mGpsLoggerServiceIntent = new Intent(this, gpsLogger.class);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //Checking if proper permissions, and if not requesting them

        showMap();

        startTrackLoggerForNewTrack();

        String fulltext = "Enregistrement tracé # "+ Long.toString(currentTrackId);
        mShowTrackId.setText(fulltext);

        mMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }
    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (mGpsLogger != null) {
            if (!mGpsLogger.isTracking()) {
                unbindService(gpsLoggerConnection);
                stopService(mGpsLoggerServiceIntent);
            } else {
                unbindService(gpsLoggerConnection);
            }
        }

        mMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void showMap(){
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mMap.setMultiTouchControls(true);
        mMap.setUseDataConnection(false);

        File file = null;
        try {
            file = getFileFromAssets("moorea.mbtiles");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            File[] fileTab = new File[1];
            fileTab[0] = file;
            OfflineTileProvider tileProvider = null;
            try {
                tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(this), fileTab);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMap.setTileProvider(tileProvider);
        }

        IMapController mapController = mMap.getController();
        mapController.setZoom(15);
        GeoPoint startPoint = new GeoPoint(-17.543859, -149.831712);
        mapController.setCenter(startPoint);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),mMap);
        mLocationOverlay.setPersonIcon(mPersonIcon);
        mLocationOverlay.enableMyLocation();
        //mLocationOverlay.enableFollowLocation();
        mMap.getOverlays().add(mLocationOverlay);


        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint myPosition = mLocationOverlay.getMyLocation();
                mMap.getController().animateTo(myPosition);
            }
        });

        btRecordTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                stopTrackLoggerForNewTrack();
                btRecordTrack.setColorFilter(Color.argb(255, 120, 120, 120));
                String fulltext = "Nb points = " + Integer.toString(mGpsLogger.getPointCount());
                mShowPointCount.setText(fulltext);
                btMyTracks.setEnabled(true);
                Toast.makeText(DisplayMapActivity.this, "Tracé enregistré. Mauururu !",Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        Intent menuActivityIntent = new Intent(DisplayMapActivity.this, MenuActivity.class);
                        startActivity(menuActivityIntent);
                    }
                },2000); //LENGTH_SHORT is usually 2 second long

            }
        });

         btShowBeacon.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v){
                 if(!IS_BEACON_SHOWING){
                     btShowBeacon.setColorFilter(Color.argb(255, 0, 255, 0));
                     IS_BEACON_SHOWING=true;
                 }else{
                     btShowBeacon.setColorFilter(Color.argb(255, 120, 120, 120));
                     IS_BEACON_SHOWING=false;
                 }
             }
         });

        /**btMyTracks.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //User clicked button
                Intent displayTrackListActivityIntent = new Intent(DisplayMapActivity.this, TrackListActivity.class);
                startActivity(displayTrackListActivityIntent);
            }
        });*/
    }

    public File getFileFromAssets(String aFileName) throws IOException {
        File cacheFile = new File(this.getCacheDir(), aFileName);
        try {
            InputStream inputStream = this.getAssets().open(aFileName);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new IOException("Could not open "+aFileName, e);
        }
        return cacheFile;
    }

    private void checkGPSProvider() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS isn't enabled. Offer user to go enable it
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_title))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_content))
                    .setCancelable(true).setPositiveButton(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton(getResources().getString(R.string.activity_display_map_dialog_GPS_disabled_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
            checkGPSFlag = false;
        }
    }

    private void startTrackLoggerForNewTrack(){
         if (checkGPSFlag){
             checkGPSProvider();
         }

         mGpsLoggerServiceIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID,currentTrackId);

         // Start GPS Logger service
         startService(mGpsLoggerServiceIntent);

         // Bind to GPS service.
         // We can't use BIND_AUTO_CREATE here, because when we'll ubound
         // later, we want to keep the service alive in background
         bindService(mGpsLoggerServiceIntent, gpsLoggerConnection, 0);
    }

    private void stopTrackLoggerForNewTrack(){
        System.out.println("TrackRecordingStopped");
        if (mGpsLogger.isTracking()) {
            Intent intent = new Intent(recopemValues.INTENT_STOP_TRACKING);
            sendBroadcast(intent);
        }
    }

    public long getCurrentTrackId() {
        return this.currentTrackId;
    }

    /**Getter for gpsLogger @return Activity */
    public gpsLogger getGpsLogger() {
        return mGpsLogger;
     }

    /**Setter for gpsLogger
     * @param l
     */
    public void setGpsLogger(gpsLogger l) {
        this.mGpsLogger = l;
     }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the fact that we are currently tracking or not
        if(mGpsLogger != null){
            outState.putBoolean(STATE_IS_TRACKING, mGpsLogger.isTracking());
            outState.putLong(TrackContentProvider.Schema.COL_TRACK_ID,currentTrackId);
        }
        super.onSaveInstanceState(outState);
    }
}
