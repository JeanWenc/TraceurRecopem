package jean.wencelius.traceurrecopem.controller;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.dataInput.dataInputGear;
import jean.wencelius.traceurrecopem.csv.ExportZip;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.ImageAdapter;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.gpx.ExportToStorageTask;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.model.ImageUrl;
import jean.wencelius.traceurrecopem.recopemValues;
import jean.wencelius.traceurrecopem.utils.MapTileProvider;
import jean.wencelius.traceurrecopem.utils.OverlayTrackPoints;

public class TrackDetailActivity extends AppCompatActivity implements ImageAdapter.OnImageListener {

    public MapView mMapView;
    public IMapController mMapViewController;

    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;

    public ContentResolver mCr;
    public Cursor mCursorPictures;
    public Cursor mCursorTrackpoints;

    public long trackId;

    public String mFisherID;

    public Boolean mPicEmpty;
    private Boolean mNewPicAdded;
    private boolean mCaughtFishDetails;
    public Boolean mDataAdded;
    public Boolean mExported;
    public Boolean mSentEmail;

    public String mSaveDir;

    private File currentImageFile;

    private static final String BUNDLE_STATE_SAVE_DIR = "stateSaveDir";
    private static final String BUNDLE_STATE_EXPORTED = "stateExported";
    private static final String BUNDLE_STATE_DATA = "stateData";
    private static final String BUNDLE_STATE_PIC = "statePic";
    private static final String BUNDLE_STATE_CAUGHT_FISH_DETAILS = "caughtFishDetails";
    private static final String BUNDLE_STATE_SENT_EMAIL = "stateSentEmail";
    private static final String BUNDLE_STATE_CURRENT_IMAGE_FILE ="stateCurrentImageFile";

    private Uri trackUri;

    private static final int REQUEST_TAKE_PHOTO = 0;
    public static final int REQUEST_BROWSE_PHOTO = 1;

    public static final String PREF_KEY_FISHER_ID = recopemValues.PREF_KEY_FISHER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        mMapView = (MapView) findViewById(R.id.activity_track_detail_display_track);
        mMapView.setMultiTouchControls(true);
        mMapView.setUseDataConnection(false);
        mMapView.setTileProvider(MapTileProvider.setMapTileProvider(getApplicationContext(),recopemValues.MAP_TILE_PROVIDER_MOOREA_SAT));
        mMapViewController = mMapView.getController();
        mMapViewController.setZoom(13);

        if(savedInstanceState!=null){
            mSaveDir = savedInstanceState.getString(BUNDLE_STATE_SAVE_DIR);
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);


            mNewPicAdded=savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
            mPicEmpty=savedInstanceState.getBoolean(BUNDLE_STATE_PIC);
            mCaughtFishDetails = savedInstanceState.getBoolean(BUNDLE_STATE_CAUGHT_FISH_DETAILS);
            mDataAdded = savedInstanceState.getBoolean(BUNDLE_STATE_DATA);
            mExported=savedInstanceState.getBoolean(BUNDLE_STATE_EXPORTED);
            mSentEmail=savedInstanceState.getBoolean(BUNDLE_STATE_SENT_EMAIL);

            String tempCurrentImageFile = savedInstanceState.getString(BUNDLE_STATE_CURRENT_IMAGE_FILE);
            if(!tempCurrentImageFile.equals("none")) currentImageFile = new File(tempCurrentImageFile);

        }else{
            mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded= getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_ADDED).equals("true");
            mPicEmpty = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_ADDED).equals("false");

            mCaughtFishDetails = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS).equals("true");

            mDataAdded = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED).equals("true");
            mExported = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_EXPORTED).equals("true");
            mSentEmail = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_SENT_EMAIL).equals("true");
        }
        setTitle("Tracé #" + trackId);

        trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

        mFisherID = AppPreferences.getDefaultsString(PREF_KEY_FISHER_ID,getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Image Gallery
        recyclerView = (RecyclerView) findViewById(R.id.activity_track_detail_recyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mCr = getContentResolver();

        mCursorPictures = mCr.query(TrackContentProvider.picturesUri(trackId), null,
                null, null, TrackContentProvider.Schema.COL_ID + " asc");

        ArrayList imageUrlList = prepareData(mCursorPictures);
        ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext(), imageUrlList, this);
        recyclerView.setAdapter(imageAdapter);

        //Map
        GeoPoint startPoint = new GeoPoint(-17.543859, -149.831712);
        mMapViewController.setCenter(startPoint);

        mCursorTrackpoints = mCr.query(TrackContentProvider.trackPointsUri(trackId), null,
                null, null, TrackContentProvider.Schema.COL_TIMESTAMP + " asc");

        if(mCursorTrackpoints.getCount()>0){
            final SimpleFastPointOverlay sfpo = OverlayTrackPoints.createPointOverlay(mCursorTrackpoints);

            mMapView.getOverlays().add(sfpo);

            final double nor = sfpo.getBoundingBox().getLatNorth();
            final double sou = sfpo.getBoundingBox().getLatSouth();
            final double eas = sfpo.getBoundingBox().getLonEast();
            final double wes = sfpo.getBoundingBox().getLonWest();
            mMapView.post(new Runnable() {
                @Override
                public void run() {
                    mMapViewController.zoomToSpan((int) (nor-sou), (int) (eas-wes));
                    mMapViewController.setCenter(new GeoPoint((nor + sou) / 2, (eas + wes) / 2));
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putString(BUNDLE_STATE_SAVE_DIR,mSaveDir);

        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putBoolean(BUNDLE_STATE_PIC,mPicEmpty);
        outState.putBoolean(BUNDLE_STATE_CAUGHT_FISH_DETAILS,mCaughtFishDetails);
        outState.putBoolean(BUNDLE_STATE_DATA,mDataAdded);
        outState.putBoolean(BUNDLE_STATE_EXPORTED,mExported);
        outState.putBoolean(BUNDLE_STATE_SENT_EMAIL,mSentEmail);


        String tempCurrentImageFile = "none";
        if(currentImageFile!=null){
            tempCurrentImageFile = currentImageFile.toString();
        }
        outState.putString(BUNDLE_STATE_CURRENT_IMAGE_FILE,tempCurrentImageFile.toString());

        super.onSaveInstanceState(outState);
    }

    private ArrayList prepareData(Cursor cursor) {
        int i=0;
        ArrayList imageUrlList = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext(),i++) {
            String imagePath = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_PATH));

            ImageUrl imageUrl = new ImageUrl();
            imageUrl.setImageUrl(imagePath);
            imageUrlList.add(imageUrl);
        }
        return imageUrlList;
    }

    @Override
    public void onImageClick(int position) {
        if(position==0) {
            getNewPictures(REQUEST_BROWSE_PHOTO);
        }else{
            Cursor c = mCursorPictures;
            c.moveToPosition(position);

            String imagePath = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_PIC_PATH));
            String imageUuid = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_UUID));

            c.close();

            Intent ShowPictureIntent = new Intent(TrackDetailActivity.this,ShowPictureActivity.class);
            ShowPictureIntent.putExtra(TrackContentProvider.Schema.COL_PIC_PATH, imagePath);
            ShowPictureIntent.putExtra(TrackContentProvider.Schema.COL_UUID,imageUuid);

            startActivity(ShowPictureIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trackdetail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.trackdetail_menu_export).setVisible(mDataAdded && (!mPicEmpty || mCaughtFishDetails));
        menu.findItem(R.id.trackdetail_menu_email).setVisible(mExported);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.trackdetail_menu_add_data:
                Intent AddDataIntent = new Intent(TrackDetailActivity.this, dataInputGear.class);
                AddDataIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                AddDataIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                startActivity(AddDataIntent);
                break;

            case R.id.trackdetail_menu_camera:
                getNewPictures(REQUEST_TAKE_PHOTO);
                break;

            case R.id.trackdetail_menu_export:
                new ExportToStorageTask(this, mSaveDir, trackId).execute();
                mExported = true;
                invalidateOptionsMenu();

                ContentValues valuesExp = new ContentValues();
                valuesExp.put(TrackContentProvider.Schema.COL_EXPORTED,"true");
                getContentResolver().update(trackUri, valuesExp, null, null);

                Toast.makeText(this, R.string.activity_track_detail_export_message_success, Toast.LENGTH_SHORT).show();
                break;
            case R.id.trackdetail_menu_email:

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                   zipAndEmail(this,mSaveDir);
                }else{
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.activity_track_detail_wifi_dialog_title))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage(getResources().getString(R.string.activity_track_detail_wifi_dialog_message))
                            .setPositiveButton(getResources().getString(R.string.activity_track_detail_wifi_dialog_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    zipAndEmail(TrackDetailActivity.this,mSaveDir);
                                }
                            }).setNegativeButton(getResources().getString(R.string.activity_track_detail_wifi_dialog_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).create().show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void zipAndEmail(Context ctx, String saveDir){
        new ExportZip(ctx,saveDir).execute();
        mSentEmail = true;
        invalidateOptionsMenu();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackContentProvider.Schema.COL_SENT_EMAIL,"true");
        getContentResolver().update(trackUri, contentValues, null, null);

    }

    private void getNewPictures(int requestType) {
        //From OsmTracker
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent getNewPictureIntent = null;

        if(requestType == REQUEST_TAKE_PHOTO){
            getNewPictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }else if(requestType == REQUEST_BROWSE_PHOTO){
            getNewPictureIntent = new Intent(Intent.ACTION_PICK);
        }


        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            getNewPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            if(requestType== REQUEST_BROWSE_PHOTO){
                getNewPictureIntent.setType("image/*");
            }
            startActivityForResult(getNewPictureIntent, requestType);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ContentValues picVal = new ContentValues();
        ContentValues trackDataPic = new ContentValues();
        File imageFile =null;

        String imageUuid = null;
        switch(requestCode) {
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    imageFile = popImageFile();
                    imageUuid = UUID.randomUUID().toString();
                    picVal.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                    picVal.put(TrackContentProvider.Schema.COL_UUID, imageUuid);
                    picVal.put(TrackContentProvider.Schema.COL_PIC_PATH, imageFile.toString());
                    getContentResolver().insert(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);
                    mNewPicAdded=true;
                    mPicEmpty = false;
                    trackDataPic.put(TrackContentProvider.Schema.COL_PIC_ADDED,"true");
                    getContentResolver().update(trackUri, trackDataPic, null, null);
                }
                break;
            case REQUEST_BROWSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //Would be useful in case of multiple image selection from Gallery
                    //ArrayList imagesEncodedList = new ArrayList<Uri>();
                    if (data.getData() != null) {
                        Uri mImageUri = data.getData();
                        // Get the cursor
                        Cursor cursor = getContentResolver().query(mImageUri,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String imageString = cursor.getString(columnIndex);

                        cursor.close();

                        imageFile = popImageFile();

                        try {
                            copyFile(new File(imageString), imageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageUuid = UUID.randomUUID().toString();

                        picVal.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                        picVal.put(TrackContentProvider.Schema.COL_UUID, imageUuid);
                        picVal.put(TrackContentProvider.Schema.COL_PIC_PATH, imageFile.toString());
                        getContentResolver().insert(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);
                        mNewPicAdded=true;
                        mPicEmpty = false;
                        trackDataPic.put(TrackContentProvider.Schema.COL_PIC_ADDED,"true");
                        getContentResolver().update(trackUri, trackDataPic, null, null);
                    }
                }
                break;
        }
        invalidateOptionsMenu();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createImageFile() throws IOException {
       currentImageFile = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = mFisherID + "_" + Long.toString(trackId) + "_" + timeStamp;

        File storageDir = new File (mSaveDir);
        // Create the track storage directory if it does not yet exist
        if (!storageDir.exists()) {
            if ( !storageDir.mkdirs() ) {
                Toast.makeText(this, "Directory [" + storageDir.getAbsolutePath() + "] does not exist and cannot be created", Toast.LENGTH_LONG).show();
            }
        }
        if (storageDir.exists() && storageDir.canWrite()) {
            currentImageFile = new File(storageDir,
                    imageFileName + DataHelper.EXTENSION_JPG);
        } else {
            Toast.makeText(this, "The directory [" + storageDir.getAbsolutePath() + "] will not allow files to be created", Toast.LENGTH_SHORT).show();
        }

        return currentImageFile;
    }

    private File popImageFile() {
        File imageFile = currentImageFile;
        currentImageFile = null;
        return imageFile;
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

}