package jean.wencelius.traceurrecopem.controller;
/**
 * TODO: Resinsert Take picture on mneu item
 * TODO: Build gallery (see website)
 * TODO: Fill in option select from gallery to populate PICTURE TBL
 */
        import androidx.annotation.Nullable;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.content.ContextCompat;
        import androidx.core.content.FileProvider;

        import android.Manifest;
        import android.app.AlertDialog;
        import android.content.ContentResolver;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.database.Cursor;
        import android.graphics.Bitmap;
        import android.graphics.Color;
        import android.media.Image;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
        import android.os.StrictMode;
        import android.provider.MediaStore;
        import android.provider.Settings;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.bumptech.glide.Glide;
        import com.bumptech.glide.RequestBuilder;
        import com.bumptech.glide.RequestManager;

        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

        import jean.wencelius.traceurrecopem.R;
        import jean.wencelius.traceurrecopem.db.DataHelper;
        import jean.wencelius.traceurrecopem.db.TrackContentProvider;
        import jean.wencelius.traceurrecopem.gpx.ExportToStorageTask;
        import jean.wencelius.traceurrecopem.model.AppPreferences;
        import jean.wencelius.traceurrecopem.model.Track;

        import static androidx.core.content.FileProvider.getUriForFile;

public class TrackDetailActivity extends AppCompatActivity {

    public ImageView mImage;
    public TextView mText;

    public long trackId;

    public String mFisherID;

    public Boolean mPicAdded;
    public Boolean mDataAdded;
    public Boolean mExported;

    public String mSaveDir;

    private File currentImageFile;

    private static final int REQUEST_TAKE_PHOTO = 0;

    public static final String PREF_KEY_FISHER_ID = "PREF_KEY_FISHER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

        mImage = (ImageView) findViewById(R.id.activity_track_detail_test);
        mText= (TextView) findViewById(R.id.activity_track_detail_text_test);

        mFisherID = AppPreferences.getDefaultsString(PREF_KEY_FISHER_ID,getApplicationContext());

        mPicAdded = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_ADDED).equals("none");
        mDataAdded = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED).equals("true");
        mExported = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_EXPORTED).equals("true");

        mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ContentResolver cr = getContentResolver();

        Cursor cPictures = cr.query(TrackContentProvider.picturesUri(trackId), null,
                null, null, TrackContentProvider.Schema.COL_ID + " asc");

        //cPictures.moveToFirst();
        //cPictures.moveToLast();
        cPictures.moveToPosition(cPictures.getCount()-1);

        String imagePath = cPictures.getString(cPictures.getColumnIndex(TrackContentProvider.Schema.COL_PIC_PATH));

        Toast.makeText(this, imagePath, Toast.LENGTH_LONG).show();

        Uri imageUri = null;
        if(cPictures.getCount()>1){
            File file = new File(imagePath);
            imageUri = Uri.fromFile(file);
        }else{
            imageUri = Uri.parse(imagePath);
        }

        RequestManager requestManager = Glide.with(this);
        RequestBuilder requestBuilder = requestManager.load(imageUri);
        requestBuilder.into(mImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trackdetail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.trackdetail_menu_camera).setVisible(mPicAdded);
        menu.findItem(R.id.trackdetail_menu_export).setVisible(!mExported);
        menu.findItem(R.id.trackdetail_menu_add_data).setVisible(!mDataAdded);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
        switch (item.getItemId()) {

            case R.id.trackdetail_menu_add_data:
                break;

            case R.id.trackdetail_menu_camera:
                getNewPictures();

                /*//TODO:The below should be conditioned on actually inputting pictures
                //TODO: Not sure button should be made invisible if people want to add more pictures
                //TODO: Action should happen on click of placeholder. Placeholder in gallery as last or first picture all the time.
                invalidateOptionsMenu();
                mPicAdded=false;

                ContentValues valuesPic = new ContentValues();
                valuesPic.put(TrackContentProvider.Schema.COL_PIC_ADDED,"true");
                getContentResolver().update(trackUri, valuesPic, null, null);*/
                break;

            case R.id.trackdetail_menu_export:
                new ExportToStorageTask(this, mSaveDir, trackId).execute();

                invalidateOptionsMenu();
                mExported = true;

                ContentValues valuesExp = new ContentValues();
                valuesExp.put(TrackContentProvider.Schema.COL_EXPORTED,"true");
                getContentResolver().update(trackUri, valuesExp, null, null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

   private void getNewPictures() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.activity_track_detail_dialog_import_pic_title))
                .setIcon(android.R.drawable.ic_menu_camera)
                .setMessage(getResources().getString(R.string.activity_track_detail_dialog_import_pic_content))
                .setCancelable(true).setPositiveButton(getResources().getString(R.string.activity_track_detail_dialog_import_pic_camera), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                takeNewPicture();
            }
        }).setNegativeButton(getResources().getString(R.string.activity_track_detail_dialog_import_pic_browse), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                browsePicture();
            }
        }).setNeutralButton(getResources().getString(R.string.activity_track_detail_dialog_import_pic_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).create().show();


    }

    private void browsePicture() {
    }

    private void takeNewPicture() {

        //From OsmTracker
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            Toast.makeText(this, photoFile.toString(), Toast.LENGTH_LONG).show();

            if (photoFile != null) {

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    File imageFile = popImageFile();
                    ContentValues picVal = new ContentValues();
                    picVal.put(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                    picVal.put(TrackContentProvider.Schema.COL_PIC_PATH, imageFile.toString());

                    Uri picUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
                    getContentResolver().insert(Uri.withAppendedPath(picUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);
                }
                break;
        }

       /* if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
           File imgFile = new File(currentImageFile.getAbsolutePath());
            if(imgFile.exists()){
                mImage.setImageURI(Uri.fromFile(imgFile));
            }

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImage.setImageBitmap(imageBitmap);
        }*/

        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createImageFile() throws IOException {
       currentImageFile = null;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        //String imageFileName = "JPEG_" + timeStamp;

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

   /* private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }*/
}