package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.ImageAdapter;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.ImageUrl;
import jean.wencelius.traceurrecopem.utils.FishPickerDialog;
import jean.wencelius.traceurrecopem.utils.MapTileProvider;

public class dataInputFishCaught extends AppCompatActivity implements ImageAdapter.OnImageListener{
    private ImageView mImageView;

    private String [] mFishFileList;
    private String [] mFishTahitianList;
    private String [] mFishFamilyList;

    private long trackId;
    private boolean mNewPicAdded;
    private String mCatchDestination;
    private String mReportedPic;

    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;

    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";
    public static final String BUNDLE_EXTRA_CATCH_DESTINATION = "catchDestination";
    public static final String BUNDLE_EXTRA_REPORTED_PIC ="reportedPic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_fish_caught);

        if(savedInstanceState!=null){
            trackId = savedInstanceState.getLong(BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(BUNDLE_STATE_NEW_PIC_ADDED);
            mCatchDestination = savedInstanceState.getString(BUNDLE_EXTRA_CATCH_DESTINATION);
            mReportedPic = savedInstanceState.getString(BUNDLE_EXTRA_REPORTED_PIC);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mCatchDestination = getIntent().getExtras().getString(BUNDLE_EXTRA_CATCH_DESTINATION);
            mReportedPic = getIntent().getExtras().getString(BUNDLE_EXTRA_REPORTED_PIC);
        }

        mFishFileList = getResources().getStringArray(R.array.data_input_fish_caught_fish_file_list);
        mFishFamilyList = getResources().getStringArray(R.array.data_input_fish_caught_fish_family_list);
        mFishTahitianList = getResources().getStringArray(R.array.data_input_fish_caught_fish_tahitian_list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mImageView = (ImageView) findViewById(R.id.image_item_id);
        recyclerView = (RecyclerView) findViewById(R.id.activity_data_input_fish_caught_recyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        ArrayList imageUrlList = prepareData(mFishFileList);
        ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext(), imageUrlList, this);
        recyclerView.setAdapter(imageAdapter);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        outState.putString(BUNDLE_EXTRA_REPORTED_PIC,mReportedPic);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onImageClick(int position) {

        FragmentManager fm = getSupportFragmentManager();
        FishPickerDialog alertDialog = FishPickerDialog.newInstance(mFishFamilyList[position],mFishTahitianList[position],(long) trackId,mCatchDestination,mReportedPic);
        alertDialog.show(fm, "fragment_alert");

        String textToDisplay = mFishFileList[position];
        Toast.makeText(this, textToDisplay, Toast.LENGTH_SHORT).show();
    }

    private ArrayList prepareData(String [] fishFileList) {

        ArrayList imageUrlList = new ArrayList<>();

        for(int i = 0; i < fishFileList.length; i++) {
            File thisImage =null;
            try {
                thisImage = MapTileProvider.getFileFromAssets(fishFileList[i], getApplicationContext());
                ImageUrl imageUrl = new ImageUrl();
                imageUrl.setImageUrl(thisImage.getAbsolutePath());
                imageUrlList.add(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageUrlList;
    }
}
