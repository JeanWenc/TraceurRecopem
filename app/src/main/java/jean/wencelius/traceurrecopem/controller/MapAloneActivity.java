package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class MapAloneActivity extends AppCompatActivity {

    /**Flag to check GPS status at startup.*/
    private boolean checkGPSFlag = true;

    private Boolean IS_BEACON_SHOWING;

    private ImageButton btCenterMap;
    private ImageButton btShowBeacon;
    MapView mMap = null;

    private MyLocationNewOverlay mLocationOverlay;

    private Bitmap mPersonIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_alone);

        mMap = (MapView) findViewById(R.id.activity_map_alone_map);

        btCenterMap = (ImageButton) findViewById(R.id.activity_map_alone_center_map);
        btShowBeacon = (ImageButton) findViewById(R.id.activity_map_alone_show_beacon);

        mPersonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);

        IS_BEACON_SHOWING=false;
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

        showMap();

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

        btShowBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(!IS_BEACON_SHOWING){
                    btShowBeacon.setColorFilter(Color.argb(255, 0, 255, 0));
                    IS_BEACON_SHOWING=true;
                }else{
                    btShowBeacon.setColorFilter(Color.argb(255, 18, 81, 140));
                    IS_BEACON_SHOWING=false;
                }
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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_map_alone_back:
                Intent menuActivityIntent = new Intent(MapAloneActivity.this, MenuActivity.class);
                startActivity(menuActivityIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
