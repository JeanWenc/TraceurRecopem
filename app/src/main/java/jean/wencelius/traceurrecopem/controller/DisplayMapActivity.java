package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import jean.wencelius.traceurrecopem.controller.service.gpsLogger;
import jean.wencelius.traceurrecopem.controller.service.gpsLoggerServiceConnection;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class DisplayMapActivity extends AppCompatActivity {

    private static final String STATE_IS_TRACKING = "isTracking";

    /**GPS Logger service, to receive events and be able to update UI.*/
    private gpsLogger mGpsLogger;

    /**GPS Logger service intent, to be used in start/stopService();*/
    private Intent mGpsLoggerServiceIntent;

    /**Flag to check GPS status at startup.*/
    private boolean checkGPSFlag = true;
    public static final int MY_DANGEROUS_PERMISSIONS_REQUESTS=42;

    /** Handles the bind to the GPS Logger service*/
    private ServiceConnection gpsLoggerConnection = new gpsLoggerServiceConnection(this);

    public static final String PREF_KEY_CURRENT_TRACK_ID = "PREF_KEY_CURRENT_TRACK_ID";

    private Boolean IS_RECORDING;
    private Boolean IS_BEACON_SHOWING;
    public int currentTrackId;
    public String currentTrackIdText;

    public TextView mShowTrackId;
    private ImageButton btCenterMap;
    private ImageButton btRecordTrack;
    private ImageButton btShowBeacon;
    private ImageButton btMyTracks;
    MapView mMap = null;

    private MyLocationNewOverlay mLocationOverlay;

    private Bitmap mPersonIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_map);

        mShowTrackId = (TextView) findViewById(R.id.activity_display_map_show_track_id);
        mMap = (MapView) findViewById(R.id.activity_display_map_map);
        btCenterMap = (ImageButton) findViewById(R.id.activity_display_map_ic_center_map);
        btRecordTrack = (ImageButton) findViewById(R.id.activity_display_map_ic_record_track);
        btMyTracks = (ImageButton) findViewById(R.id.activity_display_map_ic_my_tracks);
        btShowBeacon = (ImageButton) findViewById(R.id.activity_display_map_ic_show_beacon);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        currentTrackIdText = AppPreferences.getDefaultsString(PREF_KEY_CURRENT_TRACK_ID,getApplicationContext()) ;
        currentTrackId=Integer.parseInt(currentTrackIdText);


        IS_RECORDING = false;
        IS_BEACON_SHOWING=false;

        //Checking if proper permissions, and if not requesting them
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not yet granted => Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_DANGEROUS_PERMISSIONS_REQUESTS);
        } else {
            //Permission already granted
            showMap();
        }
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }
    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    //What happens after requesting permission? (Optional)
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_DANGEROUS_PERMISSIONS_REQUESTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    showMap();
                } else {
                    //** TODO: Gérer cette éventualité là.*/
                }
                return;
            }
        }
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
                if(!IS_RECORDING){
                    startTrackLoggerForNewTrack();

                    btRecordTrack.setColorFilter(Color.argb(255, 255, 0, 0));

                    if(currentTrackIdText!=null){
                        currentTrackId++;
                    }else{
                        currentTrackId=1;
                    }

                    String fulltext = "Enregistrement tracé # "+ Integer.toString(currentTrackId);
                    mShowTrackId.setText(fulltext);

                    IS_RECORDING=true;
                }else{
                    stopTrackLoggerForNewTrack();

                    btRecordTrack.setColorFilter(Color.argb(255, 120, 120, 120));

                    IS_RECORDING=false;

                    String currentTrackIdTextNew = Integer.toString(currentTrackId);
                    AppPreferences.setDefaultsString(PREF_KEY_CURRENT_TRACK_ID, currentTrackIdTextNew, getApplicationContext());
                }
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

        btMyTracks.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            }
        });

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
                    .setTitle("GPS Eteint")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Le GPS est éteint, il faut l'allumer.")
                    .setCancelable(true).setPositiveButton("ALLUMER GPS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
            checkGPSFlag = false;
        }
    }

    private void startTrackLoggerForNewTrack(){
        System.out.println("TrackRecordingStarted");

         if (checkGPSFlag){
             checkGPSProvider();
         }

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
        }
        super.onSaveInstanceState(outState);
    }
}
