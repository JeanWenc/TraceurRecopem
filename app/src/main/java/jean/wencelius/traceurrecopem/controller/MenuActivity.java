package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.exception.CreateTrackException;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.exception.ExportTrackException;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;
import jean.wencelius.traceurrecopem.utils.Notification_receiver;

public class MenuActivity extends AppCompatActivity {

    private Button mTrackingButton;
    private Button mAddManualTrackButton;
    private Button mMyTracksButton;
    private Button mSimpleMapButton;

    private Calendar mCalendar;

    private static final int MY_DANGEROUS_PERMISSIONS_REQUESTS=42;

    private long currentTrackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mCalendar = Calendar.getInstance();

        mTrackingButton = (Button) findViewById(R.id.activity_menu_start_tracking);
        mAddManualTrackButton = (Button) findViewById(R.id.activity_menu_start_manual_track);
        mMyTracksButton = (Button) findViewById(R.id.activity_menu_see_my_tracks);
        mSimpleMapButton = (Button) findViewById(R.id.activity_menu_see_simple_map);

        mTrackingButton.setEnabled(false);
        mAddManualTrackButton.setEnabled(false);
        mMyTracksButton.setEnabled(false);
        mSimpleMapButton.setEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not yet granted => Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_DANGEROUS_PERMISSIONS_REQUESTS);
        } else {
            AddListenersToButtons();
        }
    }

    //What happens after requesting permission? (Optional)
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_DANGEROUS_PERMISSIONS_REQUESTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AddListenersToButtons();
                } else {
                    //TODO: MENU_ACTIVITY: Gérer éventualité où utilisateur refuse autorisations.
                }
                return;
            }
        }
    }

    private void AddListenersToButtons() {
        mTrackingButton.setEnabled(true);
        mAddManualTrackButton.setEnabled(true);
        mMyTracksButton.setEnabled(true);
        mSimpleMapButton.setEnabled(true);

        mTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent i = new Intent(MenuActivity.this, MapAndTrackActivity.class);
                    // New track
                    currentTrackId = createNewTrack();

                    i.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId);
                    startActivity(i);
                }catch (CreateTrackException cte) {
                    Toast.makeText(MenuActivity.this,
                            getResources().getString(R.string.trackmgr_newtrack_error).replace("{0}", cte.getMessage()),
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
        mAddManualTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mapAloneActivityIntent = new Intent(MenuActivity.this,ManualTrackActivity.class);
                startActivity(mapAloneActivityIntent);
            }
        });

        mMyTracksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent TrackListActivityIntent = new Intent(MenuActivity.this, TrackListActivity.class);
                startActivity(TrackListActivityIntent);
            }
        });

        mSimpleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent MapAloneActivityIntent = new Intent(MenuActivity.this, MapAloneActivity.class);
                startActivity(MapAloneActivityIntent);
            }
        });
    }
    /**
     * Creates a new track, in DB and on SD card
     * @returns The ID of the new track
     * @throws CreateTrackException
     */
    private long createNewTrack() throws CreateTrackException{
        Date startDate = new Date();
        int mDay  = mCalendar.get(Calendar.DAY_OF_WEEK);

        String strYear = Integer.toString(mCalendar.get(Calendar.YEAR));
        String strMonth = Integer.toString(mCalendar.get(Calendar.MONTH)+1);
        if(strMonth.length()==1) strMonth = "0"+strMonth;
        String strDay = Integer.toString(mCalendar.get(Calendar.DATE));
        if(strDay.length()==1) strDay = "0"+strDay;
        String mSimpleDate = strYear+"-"+strMonth+"-"+strDay;

        String saveDirectory = getDataTrackDirectory(startDate);

        String fisherId = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_ID,getApplicationContext());
        String mRecopemId = fisherId + "_" + mSimpleDate;

        // Create entry in TRACK table
        ContentValues values = new ContentValues();
        values.put(TrackContentProvider.Schema.COL_NAME, "");
        values.put(TrackContentProvider.Schema.COL_INF_ID, fisherId);
        values.put(TrackContentProvider.Schema.COL_START_DATE, startDate.getTime());
        values.put(TrackContentProvider.Schema.COL_HOUR_START,"");
        values.put(TrackContentProvider.Schema.COL_HOUR_END,"");
        values.put(TrackContentProvider.Schema.COL_RECOPEM_TRACK_ID, mRecopemId);
        values.put(TrackContentProvider.Schema.COL_GPS_METHOD,"GPS");
        values.put(TrackContentProvider.Schema.COL_WEEKDAY,recopemValues.getWeekdayString(mDay));
        values.put(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED,"false");
        values.put(TrackContentProvider.Schema.COL_PIC_ADDED,"false"); // other value should be "true"
        values.put(TrackContentProvider.Schema.COL_EXPORTED,"false");
        values.put(TrackContentProvider.Schema.COL_SENT_EMAIL,"false");
        values.put(TrackContentProvider.Schema.COL_DIR,saveDirectory);
        values.put(TrackContentProvider.Schema.COL_DEVICE,android.os.Build.MODEL);
        values.put(TrackContentProvider.Schema.COL_ACTIVE, TrackContentProvider.Schema.VAL_TRACK_ACTIVE);

        Uri trackUri = getContentResolver().insert(TrackContentProvider.CONTENT_URI_TRACK, values);

        long trackId = ContentUris.parseId(trackUri);

        String defImage = "android.resource://jean.wencelius.traceurrecopem/drawable/add_picture";

        ContentValues picVal = new ContentValues();
        picVal.put(TrackContentProvider.Schema.COL_TRACK_ID,trackId);
        picVal.put(TrackContentProvider.Schema.COL_PIC_PATH,defImage);

        Uri picUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
        getContentResolver().insert(Uri.withAppendedPath(picUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);

        return trackId;
    }

    public static String getDataTrackDirectory(Date startDate){
        File sdRoot = Environment.getExternalStorageDirectory();

        // The location that the user has specified gpx files and associated content to be written
        String userGPXExportDirectoryName = recopemValues.VAL_STORAGE_DIR;

        // Create the path to the directory to which we will be writing
        String exportDirectoryPath = File.separator + userGPXExportDirectoryName.trim();
        String perTrackDirectory = File.separator + DataHelper.FILENAME_FORMATTER.format(startDate);

        String trackGPXExportDirectory = new String();
        if (android.os.Build.MODEL.equals(recopemValues.Devices.NEXUS_S)) {
            // exportDirectoryPath always starts with "/"
            trackGPXExportDirectory = exportDirectoryPath + perTrackDirectory;
        }else{
            // Create a file based on the path we've generated above
            trackGPXExportDirectory = sdRoot + exportDirectoryPath + perTrackDirectory;
        }

        File storageDir = new File(trackGPXExportDirectory);

        if (! storageDir.exists()) {
            if (! storageDir.mkdirs()) {
                //Toast.makeText(this, "Directory [" + storageDir.getAbsolutePath() + "] does not exist and cannot be created", Toast.LENGTH_LONG).show();
            }else{
                File noMedia = new File(storageDir,".nomedia");
                try {
                    FileWriter writer = new FileWriter(noMedia,false);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                }
            }
        }

        return trackGPXExportDirectory;
    }

    @Override
    public void onBackPressed() {}
}