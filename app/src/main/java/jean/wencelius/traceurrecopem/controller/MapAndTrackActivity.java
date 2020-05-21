package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.service.gpsLogger;
import jean.wencelius.traceurrecopem.service.gpsLoggerServiceConnection;
import jean.wencelius.traceurrecopem.recopemValues;
import jean.wencelius.traceurrecopem.utils.BeaconOverlay;
import jean.wencelius.traceurrecopem.utils.MapTileProvider;
import jean.wencelius.traceurrecopem.utils.WaypointNameDialog;

public class MapAndTrackActivity extends AppCompatActivity {

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
    private Boolean IS_WAYPOINTS_SHOWING;

    public long currentTrackId;

    public TextView mShowPointCount;

    private ImageButton btCenterMap;
    private ImageButton btShowBeacon;
    private ImageButton btShowWaypoints;
    private ImageButton btAddWaypoint;
    MapView mMap = null;

    private MyLocationNewOverlay mLocationOverlay;
    private ItemizedOverlayWithFocus<OverlayItem> mWaypointOverlay;

    private Bitmap mPersonIcon;

    private FolderOverlay westOverlay, eastOverlay, southOverlay, northOverlay;
    private FolderOverlay navOverlay, otherOverlay, portOverlay, starboardOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_track);

        currentTrackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

        mShowPointCount = (TextView) findViewById(R.id.activity_display_map_show_point_count);

        mMap = (MapView) findViewById(R.id.activity_display_map_map);

        //btRecordTrack = (ImageButton) findViewById(R.id.activity_display_map_ic_record_track);

        btCenterMap = (ImageButton) findViewById(R.id.activity_display_map_ic_center_map);
        btShowBeacon = (ImageButton) findViewById(R.id.activity_display_map_ic_show_beacon);
        btAddWaypoint = (ImageButton) findViewById(R.id.activity_display_map_add_waypoint);
        btShowWaypoints = (ImageButton) findViewById(R.id.activity_display_map_ic_show_my_waypoints);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        IS_BEACON_SHOWING=false;
        IS_WAYPOINTS_SHOWING = false;

        mGpsLoggerServiceIntent = new Intent(this, gpsLogger.class);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //Checking if proper permissions, and if not requesting them
        generateBeaconOverlays();
        showMap();

        startTrackLoggerForNewTrack();


        String fulltext = "Enregistrement tracé # "+ Long.toString(currentTrackId);
        setTitle(fulltext);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mapandtrack_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_map_and_track_stop_recording:
                stopTrackLoggerForNewTrack();

                String fulltext = "Nb points = " + Integer.toString(mGpsLogger.getPointCount());
                mShowPointCount.setText(fulltext);

                Toast.makeText(MapAndTrackActivity.this, "Tracé enregistré. Mauururu !",Toast.LENGTH_SHORT).show();

                item.setVisible(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        Intent menuActivityIntent = new Intent(MapAndTrackActivity.this, MenuActivity.class);
                        startActivity(menuActivityIntent);
                    }
                },2000); //LENGTH_SHORT is usually 2 second long

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMap(){
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        mMap.setMultiTouchControls(true);
        mMap.setUseDataConnection(false);
        mMap.setTileProvider(MapTileProvider.setMapTileProvider(ctx));

        IMapController mapController = mMap.getController();
        mapController.setZoom(13);
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

        btShowBeacon.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v){
                 if(!IS_BEACON_SHOWING){
                     btShowBeacon.setColorFilter(Color.argb(255, 0, 255, 0));
                     showBeacon();
                     mMap.invalidate();
                     IS_BEACON_SHOWING=true;
                 }else{
                     btShowBeacon.setColorFilter(Color.argb(255, 18, 81, 140));
                     IS_BEACON_SHOWING=false;
                     hideBeacon();
                     mMap.invalidate();
                 }
             }
        });

        btShowWaypoints.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!IS_WAYPOINTS_SHOWING){
                    btShowWaypoints.setColorFilter(Color.argb(255, 0, 255, 0));
                    showWaypoints(MapAndTrackActivity.this);
                    mMap.invalidate();
                    IS_WAYPOINTS_SHOWING=true;
                }else{
                    btShowWaypoints.setColorFilter(Color.argb(255, 18, 81, 140));
                    IS_WAYPOINTS_SHOWING=false;
                    mMap.getOverlays().remove(mWaypointOverlay);
                    mMap.invalidate();
                }
            }
        });

        btAddWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                WaypointNameDialog alertDialog = WaypointNameDialog.newInstance(currentTrackId);
                alertDialog.show(fm, "fragment_alert");

                btShowWaypoints.setColorFilter(Color.argb(255, 18, 81, 140));
                IS_WAYPOINTS_SHOWING=false;
                mMap.getOverlays().remove(mWaypointOverlay);
                mMap.invalidate();

            }
        });
        
    }

    private void generateBeaconOverlays() {
        BeaconOverlay west = new BeaconOverlay("west",getApplicationContext());
        westOverlay = (FolderOverlay)west.getKmlDocument().mKmlRoot.buildOverlay(mMap, west.getStyle(), null, west.getKmlDocument());

        BeaconOverlay east = new BeaconOverlay("east",getApplicationContext());
        eastOverlay = (FolderOverlay)east.getKmlDocument().mKmlRoot.buildOverlay(mMap, east.getStyle(), null, east.getKmlDocument());

        BeaconOverlay north = new BeaconOverlay("north",getApplicationContext());
        northOverlay = (FolderOverlay)north.getKmlDocument().mKmlRoot.buildOverlay(mMap, north.getStyle(), null, north.getKmlDocument());

        BeaconOverlay south = new BeaconOverlay("south",getApplicationContext());
        southOverlay = (FolderOverlay)south.getKmlDocument().mKmlRoot.buildOverlay(mMap, south.getStyle(), null, south.getKmlDocument());

        BeaconOverlay port = new BeaconOverlay("port",getApplicationContext());
        portOverlay = (FolderOverlay)port.getKmlDocument().mKmlRoot.buildOverlay(mMap, port.getStyle(), null, port.getKmlDocument());

        BeaconOverlay starboard = new BeaconOverlay("starboard",getApplicationContext());
        starboardOverlay = (FolderOverlay)starboard.getKmlDocument().mKmlRoot.buildOverlay(mMap, starboard.getStyle(), null, starboard.getKmlDocument());

        BeaconOverlay nav = new BeaconOverlay("nav",getApplicationContext());
        navOverlay = (FolderOverlay)nav.getKmlDocument().mKmlRoot.buildOverlay(mMap, nav.getStyle(), null, nav.getKmlDocument());

        BeaconOverlay other = new BeaconOverlay("other",getApplicationContext());
        otherOverlay = (FolderOverlay)other.getKmlDocument().mKmlRoot.buildOverlay(mMap, other.getStyle(), null, other.getKmlDocument());
    }

    private void showBeacon() {
        mMap.getOverlays().add(westOverlay);

        mMap.getOverlays().add(eastOverlay);

        mMap.getOverlays().add(northOverlay);

        mMap.getOverlays().add(southOverlay);

        mMap.getOverlays().add(portOverlay);

        mMap.getOverlays().add(starboardOverlay);

        mMap.getOverlays().add(navOverlay);

        mMap.getOverlays().add(otherOverlay);
    }

    private void hideBeacon() {
        mMap.getOverlays().remove(westOverlay);

        mMap.getOverlays().remove(eastOverlay);

        mMap.getOverlays().remove(northOverlay);

        mMap.getOverlays().remove(southOverlay);

        mMap.getOverlays().remove(portOverlay);

        mMap.getOverlays().remove(starboardOverlay);

        mMap.getOverlays().remove(navOverlay);

        mMap.getOverlays().remove(otherOverlay);
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

    public final void showWaypoints(Context ctx){

        final Context ctxt = ctx;
        List<OverlayItem> wayPointItems = new ArrayList<OverlayItem>();
        wayPointItems.clear();
        final List<String> uuidList = new ArrayList<String>();

        Cursor c = ctxt.getContentResolver().query(TrackContentProvider.waypointsUri(recopemValues.MAX_TRACK_ID),null,null,null,null);

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            OverlayItem i = new OverlayItem(
                    c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_NAME)),
                    c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_NAME)),
                    new GeoPoint(
                            c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)),
                            c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE)))
            );
            uuidList.add(c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_UUID)));
            wayPointItems.add(i);
        }

        final DataHelper mDataHelper = new DataHelper(ctxt);
        mWaypointOverlay = new ItemizedOverlayWithFocus<OverlayItem>(wayPointItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                        new AlertDialog.Builder(ctxt)
                                .setTitle(R.string.activity_map_and_track_waypoint_dialog_title)
                                .setMessage(item.getTitle().toString())
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        new AlertDialog.Builder(ctxt)
                                .setTitle(R.string.activity_map_and_track_waypoint_dialog_delete_title)
                                .setMessage(item.getTitle().toString())
                                .setPositiveButton("SUPPRIMER", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mWaypointOverlay.removeItem(index);
                                        String uuid = uuidList.get(index);
                                        mDataHelper.deleteWaypoint(uuid);
                                        uuidList.remove(index);
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                            }
                        }).create().show();
                        return true;
                    }
        },ctxt);
        c.close();
        mWaypointOverlay.setFocusItemsOnTap(false);

        mMap.getOverlays().add(mWaypointOverlay);
    }

    @Override
    public void onBackPressed() {

    }
}