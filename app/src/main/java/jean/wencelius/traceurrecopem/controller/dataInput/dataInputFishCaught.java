package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
    //TODO: First image: add new fish
    //TODO: Dialog if id = 1 make visibule input text for new fish

    private ImageView mImageView;

    private TextView mOtherFishIntro;
    private TextView mOtherFishDetail;

    private String [] mFishFileList;
    private String [] mFishTahitianList;
    private String [] mFishFamilyList;
    private String [] mFishCountList;

    public ContentResolver mCr;
    public Cursor mCursorFishCaught;

    private int mSelImage;

    private String mOtherCaughtFish;

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
    public static final String BUNDLE_STATE_LAST_SELECTED_IMAGE = "lastSelectedImage";
    public static final String BUNDLE_STATE_FISH_COUNT_LIST = "fishCountList";
    public static final String BUNDLE_STATE_OTHER_CAUGHT_FISH = "otherCaughtFish";

    private ArrayList imageUrlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_fish_caught);

        mOtherFishIntro = (TextView) findViewById(R.id.activity_data_input_fish_caught_other_fish);
        mOtherFishDetail = (TextView) findViewById(R.id.activity_data_input_fish_caught_other_fish_detail);

        mFishFileList = getResources().getStringArray(R.array.data_input_fish_caught_fish_file_list);
        mFishFamilyList = getResources().getStringArray(R.array.data_input_fish_caught_fish_family_list);
        mFishTahitianList = getResources().getStringArray(R.array.data_input_fish_caught_fish_tahitian_list);

        if(savedInstanceState!=null){
            trackId = savedInstanceState.getLong(BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(BUNDLE_STATE_NEW_PIC_ADDED);
            mCatchDestination = savedInstanceState.getString(BUNDLE_EXTRA_CATCH_DESTINATION);
            mReportedPic = savedInstanceState.getString(BUNDLE_EXTRA_REPORTED_PIC);
            mSelImage = savedInstanceState.getInt(BUNDLE_STATE_LAST_SELECTED_IMAGE);
            mFishCountList = savedInstanceState.getStringArray(BUNDLE_STATE_FISH_COUNT_LIST);
            mOtherCaughtFish = savedInstanceState.getString(BUNDLE_STATE_OTHER_CAUGHT_FISH);
        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mCatchDestination = getIntent().getExtras().getString(BUNDLE_EXTRA_CATCH_DESTINATION);
            mReportedPic = getIntent().getExtras().getString(BUNDLE_EXTRA_REPORTED_PIC);
            mSelImage = -1;

            mFishCountList = new String [mFishFileList.length];
            populateFishCountList();
        }

        if(mOtherCaughtFish.equals("")){
            mOtherFishIntro.setVisibility(View.INVISIBLE);
        }else{
            mOtherFishIntro.setVisibility(View.VISIBLE);
            mOtherFishDetail.setText(mOtherCaughtFish);
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

        setFishAdapter();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        outState.putString(BUNDLE_EXTRA_REPORTED_PIC,mReportedPic);
        outState.putInt(BUNDLE_STATE_LAST_SELECTED_IMAGE,mSelImage);
        outState.putStringArray(BUNDLE_STATE_FISH_COUNT_LIST,mFishCountList);
        outState.putString(BUNDLE_STATE_OTHER_CAUGHT_FISH,mOtherCaughtFish);

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
        Toast.makeText(this, Integer.toString(position), Toast.LENGTH_SHORT).show();
    }

    private void populateFishCountList() {
        mOtherCaughtFish =  "";

        mCr = getContentResolver();
        String selectionIn = TrackContentProvider.Schema.COL_CATCH_DESTINATION + " = ?";
        String [] selectionArgsList = {mCatchDestination};
        mCursorFishCaught = mCr.query(TrackContentProvider.poissonsUri(trackId), null,
                selectionIn, selectionArgsList, TrackContentProvider.Schema.COL_ID + " asc");

        int i =0;
        if(mCursorFishCaught.moveToFirst()){
            for(mCursorFishCaught.moveToFirst(); !mCursorFishCaught.isAfterLast(); mCursorFishCaught.moveToNext(),i++) {

                String fishFamily = mCursorFishCaught.getString(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_FISH_FAMILY));
                String fishTahitian = mCursorFishCaught.getString(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_FISH_TAHITIAN));
                int catchN = mCursorFishCaught.getInt(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N));
                String catchType = mCursorFishCaught.getString(mCursorFishCaught.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N_TYPE));

                int fishCountListIndex = Arrays.asList(mFishFamilyList).indexOf(fishFamily);
                mFishCountList[fishCountListIndex] = Integer.toString(catchN)+" "+catchType;

                if(i>mFishCountList.length) {
                    if (mOtherCaughtFish.equals("")) {
                        mOtherCaughtFish = fishTahitian + " = " + Integer.toString(catchN) + " " + catchType;
                    } else {
                        mOtherCaughtFish += " & " + fishTahitian + " = " + Integer.toString(catchN) + " " + catchType;
                    }
                }
            }
        }

        String textToDisplay = Boolean.toString(mCursorFishCaught.moveToFirst());
        Toast.makeText(this, Integer.toString(mCursorFishCaught.getCount()), Toast.LENGTH_SHORT).show();
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

    private void setFishAdapter() {
        String [] fishCountList = Arrays.copyOfRange(mFishCountList,0,mFishCountList.length);
        ImageFishAdapter imageFishAdapter = new ImageFishAdapter(getApplicationContext(), imageUrlList, fishCountList, this);
        recyclerView.setAdapter(imageFishAdapter);
    }

    public void setMyNameStr(String myCaughtFish) {

        //For Below in DialogFragment in case mSelImage ==0  make sure return from dialgo
        // returns tahitian fish name as well as catch N and catch Type.
        //Make sure in the dialog to check whether user input of Tahitian name doesn't already exist in cursor.
        // If it does already exist only update cursor, here find a way not add a new row to mFishCountList
        /*if(mSelImage == 0){
            mFishCountList[mFishCountList.length] = myCaughtFish;
            //Instead the below should be a loop going through mFishCountList index above last picture to last row of mFishCountList and progressively build...
            //?Why because if user changes value for other fish with same name needs to react to that.
            if(myCaughtFish.equals("")){
                mOtherCaughtFish = myCaughtFish;
            }else{
                mOtherCaughtFish += " & "+myCaughtFish;
            }
            mOtherFishIntro.setVisibility(View.VISIBLE);
            mOtherFishDetail.setText(mOtherCaughtFish);
*/
  //      }else{
            mFishCountList[mSelImage] = myCaughtFish;
            setFishAdapter();
    //    }
    }

}
