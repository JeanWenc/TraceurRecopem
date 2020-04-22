package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

public class TrackDetailActivity extends AppCompatActivity {

    public ImageView mImage;
    public TextView mText;

    public long currentTrackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        currentTrackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);

        mImage = (ImageView) findViewById(R.id.activity_track_detail_test);
        mText= (TextView) findViewById(R.id.activity_track_detail_text_test);

        String temp = Boolean.toString(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        String temp2 = Boolean.toString(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        mText.setText("Track # "+Long.toString(currentTrackId)+" write = "+temp+" read = "+temp2);
        //File sdRoot = Environment.getExternalStorageDirectory();
        //String exportDirectoryPath = File.separator + "RecopemTracks";
        //String perTrackDirectory = File.separator + "Track_"+ Long.toString(id);

        // Create a file based on the path we've generated above
        //File trackGPXExportDirectory = new File(sdRoot + exportDirectoryPath + perTrackDirectory);
        //trackGPXExportDirectory.mkdirs();

        String fileName = "ic_center_map.png";
        String completePath = Environment.getExternalStorageDirectory() + "/" + fileName;

        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        RequestManager requestManager = Glide.with(this);
        RequestBuilder requestBuilder = requestManager.load(imageUri);
        //RequestBuilder requestBuilder = requestManager.load(R.drawable.ic_center_map);
        requestBuilder.into(mImage);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trackdetail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
                new ExportToStorageTask(this, currentTrackId).execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
