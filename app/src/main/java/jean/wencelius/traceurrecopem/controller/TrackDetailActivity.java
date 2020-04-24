package jean.wencelius.traceurrecopem.controller;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.content.ContextCompat;

        import android.Manifest;
        import android.content.ContentResolver;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.database.Cursor;
        import android.graphics.Color;
        import android.media.Image;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.Handler;
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

        import jean.wencelius.traceurrecopem.R;
        import jean.wencelius.traceurrecopem.db.TrackContentProvider;
        import jean.wencelius.traceurrecopem.gpx.ExportToStorageTask;
        import jean.wencelius.traceurrecopem.model.AppPreferences;
        import jean.wencelius.traceurrecopem.model.Track;

public class TrackDetailActivity extends AppCompatActivity {

    public ImageView mImage;
    public TextView mText;

    public long trackId;

    public Boolean mPicAdded;
    public Boolean mDataAdded;
    public Boolean mExported;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

        mImage = (ImageView) findViewById(R.id.activity_track_detail_test);
        mText= (TextView) findViewById(R.id.activity_track_detail_text_test);

        mPicAdded = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_ADDED).equals("none");
        mDataAdded = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED).equals("true");
        mExported = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_EXPORTED).equals("true");

        /*String fileName = "ic_center_map.png";
        String completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        RequestManager requestManager = Glide.with(this);
        RequestBuilder requestBuilder = requestManager.load(imageUri);
        //RequestBuilder requestBuilder = requestManager.load(R.drawable.ic_center_map);
        requestBuilder.into(mImage);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        switch (item.getItemId()) {
            case R.id.trackdetail_menu_add_data:
                break;
            case R.id.trackdetail_menu_camera:
                break;
            case R.id.trackdetail_menu_export:
                new ExportToStorageTask(this, trackId).execute();
                invalidateOptionsMenu();
                mExported = true;

                ContentValues values = new ContentValues();
                values.put(TrackContentProvider.Schema.COL_EXPORTED,"true");

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
                getContentResolver().update(trackUri, values, null, null);

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

