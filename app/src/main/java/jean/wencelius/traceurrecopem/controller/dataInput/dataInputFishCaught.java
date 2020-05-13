package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.MapAloneActivity;
import jean.wencelius.traceurrecopem.controller.MenuActivity;
import jean.wencelius.traceurrecopem.db.ImageAdapter;
import jean.wencelius.traceurrecopem.db.ImageFishAdapter;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.ImageUrl;
import jean.wencelius.traceurrecopem.utils.FishPickerDialog;
import jean.wencelius.traceurrecopem.utils.MapTileProvider;

public class dataInputFishCaught extends AppCompatActivity implements ImageFishAdapter.OnImageListener{
    //TODO: on start populate mFishCountList from Cursor because if go out of fishCaught and Back fishCountList is empty.

    private ImageView mImageView;

    private String [] mFishFileList;
    private String [] mFishTahitianList;
    private String [] mFishFamilyList;
    private String [] mFishCountList;

    private long trackId;
    private boolean mNewPicAdded;
    private String mCatchDestination;
    private String mReportedPic;

    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;

    private int mSelImage;
    private String mDialogReturn;

    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";
    public static final String BUNDLE_EXTRA_CATCH_DESTINATION = "catchDestination";
    public static final String BUNDLE_EXTRA_REPORTED_PIC ="reportedPic";
    public static final String BUNDLE_STATE_DIALOG_RETURN="dialogReturn";
    public static final String BUNDLE_STATE_LAST_SELECTED_IMAGE = "lastSelectedImage";
    public static final String BUNDLE_STATE_FISH_COUNT_LIST = "fishCountList";

    private ArrayList imageUrlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_fish_caught);

        mFishFileList = getResources().getStringArray(R.array.data_input_fish_caught_fish_file_list);
        mFishFamilyList = getResources().getStringArray(R.array.data_input_fish_caught_fish_family_list);
        mFishTahitianList = getResources().getStringArray(R.array.data_input_fish_caught_fish_tahitian_list);

        if(savedInstanceState!=null){
            trackId = savedInstanceState.getLong(BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(BUNDLE_STATE_NEW_PIC_ADDED);
            mCatchDestination = savedInstanceState.getString(BUNDLE_EXTRA_CATCH_DESTINATION);
            mReportedPic = savedInstanceState.getString(BUNDLE_EXTRA_REPORTED_PIC);
            mSelImage = savedInstanceState.getInt(BUNDLE_STATE_LAST_SELECTED_IMAGE);
            mDialogReturn = savedInstanceState.getString(BUNDLE_STATE_DIALOG_RETURN);
            mFishCountList = savedInstanceState.getStringArray(BUNDLE_STATE_FISH_COUNT_LIST);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mCatchDestination = getIntent().getExtras().getString(BUNDLE_EXTRA_CATCH_DESTINATION);
            mReportedPic = getIntent().getExtras().getString(BUNDLE_EXTRA_REPORTED_PIC);
            mSelImage = -1;
            mDialogReturn="";

            mFishCountList = new String [mFishFileList.length];
            //After the line above Load Fish Caught Cursor (query should be done on trackId + mCatchDestination)
            //Create extra method like prepareData: that populates mFishCountList with data in cursor if any.
            //for each cursor position  :  index of  mFishFamilyList equal to cursor.fishNameID
            //mFishCountList[indexabove] = cursor.fishNameCatch
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mImageView = (ImageView) findViewById(R.id.image_fish_item_id);
        recyclerView = (RecyclerView) findViewById(R.id.activity_data_input_fish_caught_recyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        imageUrlList = prepareData(mFishFileList);

        //ALL OF THE BELOW IN METHOD SET ADAPTER so that it can be called in last method setMyNameStr
        ImageFishAdapter imageFishAdapter = new ImageFishAdapter(getApplicationContext(), imageUrlList, mFishCountList, this);
        recyclerView.setAdapter(imageFishAdapter);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        outState.putString(BUNDLE_EXTRA_REPORTED_PIC,mReportedPic);
        outState.putInt(BUNDLE_STATE_LAST_SELECTED_IMAGE,mSelImage);
        outState.putString(BUNDLE_STATE_DIALOG_RETURN,mDialogReturn);
        outState.putStringArray(BUNDLE_STATE_FISH_COUNT_LIST,mFishCountList);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fishcaught_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_data_input_fish_caught_back:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageClick(int position) {

        mSelImage = position;

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

    public void setMyNameStr(String myNameStr) {
        mFishCountList[mSelImage] = myNameStr;
        ImageFishAdapter imageFishAdapter = new ImageFishAdapter(getApplicationContext(), imageUrlList, mFishCountList, this);
        recyclerView.setAdapter(imageFishAdapter);
    }

}
