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
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputBoat extends AppCompatActivity {

    static dataInputBoat boatAct;

    public String mBoat;
    public String mBoatOwner;

    private long trackId;
    private boolean mNewPicAdded;
    private String mSaveDir;


    private CheckBox mCheckBox;

    private static final String BUNDLE_STATE_BOAT = "boatType";
    private static final String BUNDLE_STATE_BOAT_OWNER = "boatOwner";

    private boolean showNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_boat);

        boatAct = this;

        mCheckBox = (CheckBox) findViewById(R.id.activity_data_input_boat_boat_owner);

        RadioButton radioMotorboat = (RadioButton) findViewById(R.id.activity_data_input_boat_motor);
        RadioButton radioOutrigger = (RadioButton) findViewById(R.id.activity_data_input_boat_pirogue);
        RadioButton radioSwim = (RadioButton) findViewById(R.id.activity_data_input_boat_nage);
        RadioButton radioShore = (RadioButton) findViewById(R.id.activity_data_input_boat_shore);

        if(savedInstanceState!=null){
            showNext = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON);
            mBoat = savedInstanceState.getString(BUNDLE_STATE_BOAT);
            mBoatOwner = savedInstanceState.getString(BUNDLE_STATE_BOAT_OWNER);
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
            mSaveDir=savedInstanceState.getString(recopemValues.BUNDLE_STATE_SAVE_DIR);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK,trackId),null,null,null,null);
            mTrackCursor.moveToPosition(0);
            String boat = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_BOAT));
            String boatOwner = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_BOAT_OWNER));
            mTrackCursor.close();

            if(boat==null){
                boat = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT,getApplicationContext());
                boatOwner = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT_OWNER,getApplicationContext());
            }

            if(null != boat){
                mBoat = boat;
                mBoatOwner = boatOwner;
            }else{
                mBoat = "empty";
                mBoatOwner = "NA";
            }
        }

        if(mBoat.equals("empty")){
            showNext = false;
        }else if(mBoat.equals("motorboat") || mBoat.equals("outrigger")){
            radioMotorboat.setChecked(mBoat.equals("motorboat"));
            radioOutrigger.setChecked(mBoat.equals("outrigger"));
            mCheckBox.setVisibility((View.VISIBLE));
            mCheckBox.setChecked(mBoatOwner.equals("true"));
            showNext = true;
        }else{
            radioShore.setChecked(mBoat.equals("from_shore"));
            radioSwim.setChecked(mBoat.equals("swim"));
            mCheckBox.setVisibility(View.INVISIBLE);
            showNext = true;
        }

        invalidateOptionsMenu();

        setTitle("Question 2/8");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_BOAT,mBoat);
        outState.putString(BUNDLE_STATE_BOAT_OWNER,mBoatOwner);
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,showNext);
        outState.putString(recopemValues.BUNDLE_STATE_SAVE_DIR,mSaveDir);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);

        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        mCheckBox.setChecked(false);
        switch (view.getId()) {
            case R.id.activity_data_input_boat_motor:
                if (checked){
                    mBoat = "motorboat";
                    mCheckBox.setVisibility(View.VISIBLE);
                    mCheckBox.setText(getResources().getString(R.string.data_input_boat_question_boat_owner));
                    mBoatOwner="false";
                }
                break;
            case R.id.activity_data_input_boat_pirogue:
                if (checked){
                    mBoat = "outrigger";
                    mCheckBox.setVisibility(View.VISIBLE);
                    mCheckBox.setText(getResources().getString(R.string.data_input_boat_question_boat_owner_outrigger));
                    mBoatOwner="false";
                }
                break;
            case R.id.activity_data_input_boat_nage:
                if (checked){
                    mBoat = "swim";
                    mCheckBox.setVisibility(View.INVISIBLE);
                    mBoatOwner="NA";
                }
                break;
            case R.id.activity_data_input_boat_shore:
                if (checked){
                    mBoat = "from_shore";
                    mCheckBox.setVisibility(View.INVISIBLE);
                    mBoatOwner="NA";
                }
                break;
        }
        showNext = true;
        invalidateOptionsMenu();
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.activity_data_input_boat_boat_owner:
                if (checked) {
                    mBoatOwner = "true";
                } else {
                    mBoatOwner = "false";
                }
                break;
        }
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

                ContentValues boatValues = new ContentValues();
                boatValues.put(TrackContentProvider.Schema.COL_BOAT,mBoat);
                boatValues.put(TrackContentProvider.Schema.COL_BOAT_OWNER,mBoatOwner);

                getContentResolver().update(trackUri, boatValues, null, null);

                Intent NextIntent = new Intent(dataInputBoat.this, dataInputCrew.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_DIR,mSaveDir);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputBoat getInstance(){
        return   boatAct;
    }
}
