package jean.wencelius.traceurrecopem.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;


public class dataInputWind extends AppCompatActivity {

    static dataInputWind windAct;

    public String mWindEstFisher;
    public String mCurrentEstFisher;

    private long trackId;
    private boolean mNewPicAdded;
    private String mSaveDir;

    private boolean showNext;

    private boolean cg1;
    private boolean cg2;

    private static final String BUNDLE_STATE_WIND = "wind";
    private static final String BUNDLE_STATE_CURRENT = "current";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_wind);

        windAct = this;

        if(savedInstanceState!=null){
            mWindEstFisher = savedInstanceState.getString(BUNDLE_STATE_WIND);
            mCurrentEstFisher = savedInstanceState.getString(BUNDLE_STATE_CURRENT);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
            mSaveDir = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SAVE_DIR);
        }else {
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);
            String windEstFisher = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_WIND_FISHER));
            String currentEstFisher = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CURRENT_FISHER));
            mTrackCursor.close();

            if (windEstFisher != null) {
                mWindEstFisher = windEstFisher;
                mCurrentEstFisher = currentEstFisher;
            } else {
                mWindEstFisher = "empty";
                mCurrentEstFisher = "empty";
            }
        }

        if(!mWindEstFisher.equals("empty")){
            RadioButton mRbWind;
            if(mWindEstFisher.equals("low")){
                mRbWind = (RadioButton) findViewById(R.id.activity_data_input_wind_1);
            }else if(mWindEstFisher.equals("mid")){
                mRbWind = (RadioButton) findViewById(R.id.activity_data_input_wind_2);
            }else{
                mRbWind = (RadioButton) findViewById(R.id.activity_data_input_wind_3);
            }
            mRbWind.setChecked(true);
        }
        if(!mCurrentEstFisher.equals("empty")){
            RadioButton mRbCurrent;

            if(mCurrentEstFisher.equals("low")){
                mRbCurrent = (RadioButton) findViewById(R.id.activity_data_input_current_1);
            }else if(mCurrentEstFisher.equals("mid")){
                mRbCurrent = (RadioButton) findViewById(R.id.activity_data_input_current_2);
            }else{
                mRbCurrent = (RadioButton) findViewById(R.id.activity_data_input_current_3);
            }
            mRbCurrent.setChecked(true);
        }

        cg1 = !mWindEstFisher.equals("empty");
        cg2 = !mCurrentEstFisher.equals("empty");

        showNext = cg1 && cg2;
        invalidateOptionsMenu();

        setTitle("Question 4/8");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_WIND,mWindEstFisher);
        outState.putString(BUNDLE_STATE_CURRENT,mCurrentEstFisher);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(recopemValues.BUNDLE_STATE_SAVE_DIR,mSaveDir);
        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_wind_1:
                if (checked) {
                    mWindEstFisher = "low";
                    cg1=true;
                }
                break;
            case R.id.activity_data_input_wind_2:
                if (checked) {
                    mWindEstFisher = "mid";
                    cg1=true;
                }
                break;
            case R.id.activity_data_input_wind_3:
                if (checked) {
                    mWindEstFisher = "high";
                    cg1=true;
                }
                break;
            case R.id.activity_data_input_current_1:
                if (checked) {
                    mCurrentEstFisher = "low";
                    cg2=true;
                }
                break;
            case R.id.activity_data_input_current_2:
                if (checked) {
                    mCurrentEstFisher = "mid";
                    cg2=true;
                }
                break;
            case R.id.activity_data_input_current_3:
                if (checked) {
                    mCurrentEstFisher = "high";
                    cg2=true;
                }
                break;
        }
        showNext = cg1 && cg2;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.datainput_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_data_input_menu_next).setVisible(showNext);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.activity_data_input_menu_next:
                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues windValues = new ContentValues();
                windValues.put(TrackContentProvider.Schema.COL_WIND_FISHER,mWindEstFisher);
                windValues.put(TrackContentProvider.Schema.COL_CURRENT_FISHER,mCurrentEstFisher);

                getContentResolver().update(trackUri, windValues, null, null);

                Intent NextIntent = new Intent(dataInputWind.this, dataInputCatchSale.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_DIR,mSaveDir);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputWind getInstance(){
        return   windAct;
    }
}
