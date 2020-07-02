package jean.wencelius.traceurrecopem.controller;

import androidx.annotation.NonNull;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
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
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.util.ArrayList;
import java.util.List;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.service.gpsLogger;
import jean.wencelius.traceurrecopem.service.gpsLoggerServiceConnection;
import jean.wencelius.traceurrecopem.recopemValues;
import jean.wencelius.traceurrecopem.utils.BeaconOverlay;
import jean.wencelius.traceurrecopem.utils.MapTileProvider;
import jean.wencelius.traceurrecopem.utils.OverlayTrackPoints;
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

    public long currentTrackId;

    public TextView mShowPointCount;

    private ImageButton btCenterMap;
    private ImageButton btShowBeacon;
    private ImageButton btShowWaypoints;
    private ImageButton btAddWaypoint;
    private ImageButton btShowCurrentTrack;

    private Boolean IS_BEACON_SHOWING;
    private Boolean IS_WAYPOINTS_SHOWING;
    private Boolean IS_CURRENT_TRACK_SHOWING;
    private String selTileProvider;

    private double mZoomLevel, mCurrentLon, mCurrentLat;
    private final static double mMooreaCenterLon = -149.831712;
    private final static double mMooreaCenterLat = -17.543859;

    MapView mMap = null;
    private IMapController mapController;

    private Bitmap mPersonIcon;

    private MyLocationNewOverlay mLocationOverlay;
    private ItemizedOverlayWithFocus<OverlayItem> mWaypointOverlay;
    private FolderOverlay westOverlay, eastOverlay, southOverlay, northOverlay;
    private FolderOverlay navOverlay, otherOverlay, portOverlay, starboardOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_track);

        mShowPointCount = (TextView) findViewById(R.id.activity_display_map_show_point_count);

        mMap = (MapView) findViewById(R.id.activity_display_map_map);

        btCenterMap = (ImageButton) findViewById(R.id.activity_display_map_ic_center_map);
        btShowBeacon = (ImageButton) findViewById(R.id.activity_display_map_ic_show_beacon);
        btAddWaypoint = (ImageButton) findViewById(R.id.activity_display_map_add_waypoint);
        btShowWaypoints = (ImageButton) findViewById(R.id.activity_display_map_ic_show_my_waypoints);
        btShowCurrentTrack = (ImageButton) findViewById(R.id.activity_display_map_show_current_track);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        if(null!=savedInstanceState){
            currentTrackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            IS_BEACON_SHOWING = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_SHOW_BEACON);
            IS_WAYPOINTS_SHOWING = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_SHOW_WAYPOINTS);
            IS_CURRENT_TRACK_SHOWING = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_SHOW_CURRENT_TRACK);
            mZoomLevel = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_ZOOM);
            mCurrentLon = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_LONGITUDE);
            mCurrentLat = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_LATITUDE);
            selTileProvider = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SEL_TILE_PROVIDER);
        }else{
            currentTrackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            IS_BEACON_SHOWING=false;
            IS_WAYPOINTS_SHOWING = false;
            IS_CURRENT_TRACK_SHOWING = false;
            mZoomLevel = 13.0;
            mCurrentLat=mMooreaCenterLat;
            mCurrentLon=mMooreaCenterLon;
            selTileProvider = recopemValues.MAP_TILE_PROVIDER_MOOREA_SAT;
        }

        mGpsLoggerServiceIntent = new Intent(this, gpsLogger.class);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,currentTrackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_SHOW_BEACON,IS_BEACON_SHOWING);
        outState.putBoolean(recopemValues.BUNDLE_STATE_SHOW_WAYPOINTS,IS_WAYPOINTS_SHOWING);
        outState.putBoolean(recopemValues.BUNDLE_STATE_SHOW_CURRENT_TRACK,IS_CURRENT_TRACK_SHOWING);
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_ZOOM,mMap.getZoomLevelDouble());
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_LATITUDE,mMap.getMapCenter().getLatitude());
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_LONGITUDE,mMap.getMapCenter().getLongitude());
        outState.putString(recopemValues.BUNDLE_STATE_SEL_TILE_PROVIDER,selTileProvider);
        if(mGpsLogger != null){
            outState.putBoolean(STATE_IS_TRACKING, mGpsLogger.isTracking());
        }
        //outState.putInt(BUNDLE_STATE_MLINE_INDEX,mLineIndex);
        super.onSaveInstanceState(outState);
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
        if(!AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_NAME,getApplicationContext()).equals(recopemValues.USER_NAME_JEROME)) menu.findItem(R.id.activity_map_and_track_jerome).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_map_and_track_stop_recording:
                stopTrackLoggerForNewTrack();

                String fulltext = "Nb points = " + Integer.toString(mGpsLogger.getPointCount());
                mShowPointCount.setText(fulltext);

                Toast.makeText(MapAndTrackActivity.this, R.string.thank_you_message,Toast.LENGTH_SHORT).show();

                item.setVisible(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        Intent menuActivityIntent = new Intent(MapAndTrackActivity.this, MenuActivity.class);
                        startActivity(menuActivityIntent);
                    }
                },2000); //LENGTH_SHORT is usually 2 second long

                break;
            case R.id.activity_map_and_track_jerome:
                if (selTileProvider.equals(recopemValues.MAP_TILE_PROVIDER_MOOREA_SAT)){
                    selTileProvider = recopemValues.MAP_TILE_PROVIDER_NAVIONICS;
                    mMap.setTileProvider(MapTileProvider.setMapTileProvider(getApplicationContext(),selTileProvider));
                }else{
                    selTileProvider = recopemValues.MAP_TILE_PROVIDER_MOOREA_SAT;
                    mMap.setTileProvider(MapTileProvider.setMapTileProvider(getApplicationContext(),selTileProvider));
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMap(){
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        mMap.setMultiTouchControls(true);
        mMap.setUseDataConnection(false);
        mMap.setTileProvider(MapTileProvider.setMapTileProvider(ctx,selTileProvider));

        mapController = mMap.getController();
        mapController.setZoom(mZoomLevel);

       GeoPoint startPoint = new GeoPoint(mCurrentLat, mCurrentLon);
       mapController.setCenter(startPoint);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),mMap);
        mLocationOverlay.setPersonIcon(mPersonIcon);
        mLocationOverlay.enableMyLocation();
        //mLocationOverlay.enableFollowLocation();
        mMap.getOverlays().add(mLocationOverlay);

        if(IS_BEACON_SHOWING) {
            hideBeacon();
            mMap.invalidate();
            btShowBeacon.setColorFilter(Color.argb(255, 0, 255, 0));
            generateBeaconOverlays();
            showBeacon();
            mMap.invalidate();
        }
        if(IS_WAYPOINTS_SHOWING){
            mMap.getOverlays().remove(mWaypointOverlay);
            btShowWaypoints.setColorFilter(Color.argb(255, 0, 255, 0));
            showWaypoints(MapAndTrackActivity.this);
            mMap.invalidate();
        }
        if(IS_CURRENT_TRACK_SHOWING){
            showCurrentTrack();
        }

        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint myPosition = mLocationOverlay.getMyLocation();
                mMap.getController().animateTo(myPosition);
            }
        });

        btShowCurrentTrack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showCurrentTrack();
                IS_CURRENT_TRACK_SHOWING=true;
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

    public void showCurrentTrack(){
        Cursor c = getContentResolver().query(TrackContentProvider.trackPointsUri(currentTrackId),null,null,null,null);
        if(c.getCount()>0){
            final SimpleFastPointOverlay sfpo = OverlayTrackPoints.createPointOverlay(c);

            mMap.getOverlays().add(sfpo);

            final double nor = sfpo.getBoundingBox().getLatNorth();
            final double sou = sfpo.getBoundingBox().getLatSouth();
            final double eas = sfpo.getBoundingBox().getLonEast();
            final double wes = sfpo.getBoundingBox().getLonWest();
            c.moveToLast();
            final GeoPoint lastGeoPoint = new GeoPoint(c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)),c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE)));
            mMap.post(new Runnable() {
                @Override
                public void run() {
                    mapController.zoomToSpan((int) (nor-sou), (int) (eas-wes));
                    mapController.setCenter(lastGeoPoint);
                    //mapController.setCenter(new GeoPoint((nor + sou) / 2, (eas + wes) / 2));
                }
            });
        }
        c.close();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mGpsLoggerServiceIntent);
        }else{
            startService(mGpsLoggerServiceIntent);
        }

        // Bind to GPS service.
         // We can't use BIND_AUTO_CREATE here, because when we'll ubound
         // later, we want to keep the service alive in background
         bindService(mGpsLoggerServiceIntent, gpsLoggerConnection, 0);
    }

    private void stopTrackLoggerForNewTrack(){
        Intent intent = new Intent(recopemValues.INTENT_STOP_TRACKING);
        sendBroadcast(intent);
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
    public void onBackPressed() {

    }
}