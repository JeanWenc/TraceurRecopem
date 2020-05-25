package jean.wencelius.traceurrecopem.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PolyOverlayWithIW;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;
import jean.wencelius.traceurrecopem.utils.BeaconOverlay;
import jean.wencelius.traceurrecopem.utils.MapTileProvider;

public class MapAloneActivity extends AppCompatActivity {

    /**Flag to check GPS status at startup.*/
    private boolean checkGPSFlag = true;

    private boolean createNewTrack;
    private long newTrackId;

    private ContentObserver trackpointContentObserver;

    private Boolean IS_BEACON_SHOWING;
    private Boolean IS_WAYPOINTS_SHOWING;

    private ImageButton btCenterMap;
    private ImageButton btShowBeacon;
    private ImageButton btShowWaypoints;

    MapView mMap = null;

    private double mZoomLevel, mCurrentLon, mCurrentLat;
    private final static double mMooreaCenterLon=-149.831712;
    private final static double mMooreaCenterLat=-17.543859;


    private Bitmap mPersonIcon;
    private MyLocationNewOverlay mLocationOverlay;
    private ItemizedOverlayWithFocus<OverlayItem> mWaypointOverlay;
    private Polyline mLine;
    private Marker mFirstPoint;

    private int mLineIndex;
    private FolderOverlay westOverlay, eastOverlay, southOverlay, northOverlay;
    private FolderOverlay navOverlay, otherOverlay, portOverlay, starboardOverlay;

    private static final String BUNDLE_STATE_CREATE_NEW_TRACK = "createNewTrack";
    private static final String BUNDLE_STATE_CREATE_NEW_TRACK_ID = "createNewTrackId";
    public static final String BUNDLE_STATE_MLINE_INDEX ="mLineIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_alone);

        mMap = (MapView) findViewById(R.id.activity_map_alone_map);

        btCenterMap = (ImageButton) findViewById(R.id.activity_map_alone_center_map);
        btShowBeacon = (ImageButton) findViewById(R.id.activity_map_alone_show_beacon);
        btShowWaypoints = (ImageButton) findViewById(R.id.activity_map_alone_show_my_waypoints);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        if(null!=savedInstanceState){
            IS_BEACON_SHOWING = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_SHOW_BEACON);
            IS_WAYPOINTS_SHOWING = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_SHOW_WAYPOINTS);

            mZoomLevel = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_ZOOM);
            mCurrentLon = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_LONGITUDE);
            mCurrentLat = savedInstanceState.getDouble(recopemValues.BUNDLE_STATE_CURRENT_LATITUDE);

            mLineIndex = savedInstanceState.getInt(BUNDLE_STATE_MLINE_INDEX);

            createNewTrack = savedInstanceState.getBoolean(BUNDLE_STATE_CREATE_NEW_TRACK);
            newTrackId = savedInstanceState.getLong(BUNDLE_STATE_CREATE_NEW_TRACK_ID);
        }else{
            mZoomLevel = 13.0;
            mCurrentLat=mMooreaCenterLat;
            mCurrentLon=mMooreaCenterLon;

            IS_BEACON_SHOWING=false;
            IS_WAYPOINTS_SHOWING = false;

            mLineIndex=-1;

            Bundle extras = getIntent().getExtras();
            if(extras!=null){
                createNewTrack = extras.getString(recopemValues.BUNDLE_EXTRA_CREATE_MANUAL_TRACK).equals("true");
                newTrackId = extras.getLong(recopemValues.BUNDLE_EXTRA_CREATE_MANUAL_TRACK_ID);
            }else{
                createNewTrack = false;
                newTrackId = (long) -1;
            }
        }

        if(createNewTrack){

            final MapEventsReceiver mReceive = new MapEventsReceiver(){
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    return false;
                }
                @Override
                public boolean longPressHelper(GeoPoint p) {
                    String trackPointUuid = UUID.randomUUID().toString();

                    ContentValues values = new ContentValues();
                    values.put(TrackContentProvider.Schema.COL_TRACK_ID, newTrackId);
                    values.put(TrackContentProvider.Schema.COL_LATITUDE, p.getLatitude());
                    values.put(TrackContentProvider.Schema.COL_LONGITUDE, p.getLongitude());
                    values.put(TrackContentProvider.Schema.COL_UUID,trackPointUuid);
                    values.put(TrackContentProvider.Schema.COL_TIMESTAMP,(long) 0);

                    getContentResolver().insert(TrackContentProvider.trackPointsUri(newTrackId), values);
                    getContentResolver().insert(TrackContentProvider.trackPointsUri(newTrackId), values);
                    return false;
                }
            };
            mMap.getOverlays().add(new MapEventsOverlay(mReceive));

            // Create content observer for trackpoints
            trackpointContentObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    pathChanged();
                }
            };
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_ZOOM,mMap.getZoomLevelDouble());
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_LATITUDE,mMap.getMapCenter().getLatitude());
        outState.putDouble(recopemValues.BUNDLE_STATE_CURRENT_LONGITUDE,mMap.getMapCenter().getLongitude());

        outState.putBoolean(recopemValues.BUNDLE_STATE_SHOW_BEACON,IS_BEACON_SHOWING);
        outState.putBoolean(recopemValues.BUNDLE_STATE_SHOW_WAYPOINTS,IS_WAYPOINTS_SHOWING);

        outState.putInt(BUNDLE_STATE_MLINE_INDEX,mLineIndex);

        outState.putBoolean(BUNDLE_STATE_CREATE_NEW_TRACK,createNewTrack);
        outState.putLong(BUNDLE_STATE_CREATE_NEW_TRACK_ID,newTrackId);
        super.onSaveInstanceState(outState);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //Checking if proper permissions, and if not requesting them
        if (checkGPSFlag){
            checkGPSProvider();
        }
        generateBeaconOverlays();

        showMap();
        if(createNewTrack) resumeActivity();

        mMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    private void resumeActivity(){
        // Register content observer for any trackpoint changes
        getContentResolver().registerContentObserver(
                TrackContentProvider.trackPointsUri(newTrackId),
                true, trackpointContentObserver);

        // Reload path
        pathChanged();
    }

    public void onPause(){
        // Unregister content observer
        if(createNewTrack) getContentResolver().unregisterContentObserver(trackpointContentObserver);
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mMap.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void showMap(){
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mMap.setMultiTouchControls(true);
        mMap.setUseDataConnection(false);
        mMap.setTileProvider(MapTileProvider.setMapTileProvider(ctx));

        IMapController mapController = mMap.getController();
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
            showWaypoints(MapAloneActivity.this);
            mMap.invalidate();
        }

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
                    showWaypoints(MapAloneActivity.this);
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
    }

    private void pathChanged() {
        hideManualTrack();
        List<GeoPoint> geoPoints = new ArrayList<>();
        List<GeoPoint> firstPoint = new ArrayList<>();
        Cursor c = getContentResolver().query(
                TrackContentProvider.trackPointsUri(newTrackId),
                null, null, null, TrackContentProvider.Schema.COL_ID + " asc");

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            GeoPoint i = new  GeoPoint(
                            c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)),
                            c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE))
            );
            geoPoints.add(i);
            if(geoPoints.size()==1){
                firstPoint.add(i);
                mFirstPoint = new Marker(mMap);
                mFirstPoint.setPosition(firstPoint.get(0));
                mMap.getOverlays().add(mFirstPoint);
            }
        }
        c.close();


        mLine = new Polyline();   //see note below!
        mLine.setPoints(geoPoints);
        mLine.setColor(Color.RED);
        geoPoints.clear();
        firstPoint.clear();
        mMap.getOverlayManager().add(mLine);
        mLineIndex = mMap.getOverlays().indexOf(mLine);
    }

    private void hideManualTrack(){
        mMap.getOverlays().remove(mFirstPoint);
        mMap.getOverlays().remove(mLine);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mapalone_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_map_alone_back).setVisible(!createNewTrack);
        menu.findItem(R.id.activity_map_alone_delete_last_trackpoint).setVisible(createNewTrack);
        menu.findItem(R.id.activity_map_alone_done).setVisible(createNewTrack);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_map_alone_back:
                if(createNewTrack==false){
                    Intent menuActivityIntent = new Intent(MapAloneActivity.this, MenuActivity.class);
                    startActivity(menuActivityIntent);
                }
                break;
            case R.id.activity_map_alone_delete_last_trackpoint:
                Cursor c = getContentResolver().query(
                        TrackContentProvider.trackPointsUri(newTrackId),
                        null, null, null, TrackContentProvider.Schema.COL_ID + " asc");
                final DataHelper mDataHelper = new DataHelper(this);
                String targetUuid="";
                if(c.moveToFirst()){
                    c.moveToLast();
                    targetUuid = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_UUID));
                    mDataHelper.deleteTrackpoint(targetUuid);
                    c.moveToLast();
                    targetUuid = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_UUID));
                    mDataHelper.deleteTrackpoint(targetUuid);
                }
                c.close();
                pathChanged();
                break;
            case R.id.activity_map_alone_done:
                Toast.makeText(this, R.string.thank_you_message, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        Intent TrackListActivityIntent = new Intent(MapAloneActivity.this, TrackListActivity.class);
                        startActivity(TrackListActivityIntent);
                    }
                },2000); //LENGTH_SHORT is usually 2 second long


        }
        return super.onOptionsItemSelected(item);
    }
}
