package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.gpx.ExportToStorageTask;

public class ShowPictureActivity extends AppCompatActivity {

    private ImageView mImage;
    private String mImagePath;
    private String mImageUuid;
    private DataHelper mDataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        mDataHelper = new DataHelper(this);

        mImage = findViewById(R.id.activity_show_picture_image);

        mImagePath = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_PIC_PATH);
        mImageUuid = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_UUID);
        File imageFile = new File(mImagePath);
        Uri imageUri = Uri.fromFile(imageFile);

        Glide.with(this).load(imageUri).into(mImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.showpicture_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
        switch (item.getItemId()) {

            case R.id.showpicture_menu_delete:
                String uuid = mImageUuid;
                mDataHelper.deletePicture(uuid);

                File imageFile = new File(mImagePath);
                boolean deleted = imageFile.delete();

                Toast.makeText(this, R.string.activity_show_picture_deleting_image, Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){
                        Intent TrackDetailIntent = new Intent(ShowPictureActivity.this,TrackDetailActivity.class);
                        startActivity(TrackDetailIntent);
                    }
                },1000); //LENGTH_SHORT is usually 2 second long


                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
