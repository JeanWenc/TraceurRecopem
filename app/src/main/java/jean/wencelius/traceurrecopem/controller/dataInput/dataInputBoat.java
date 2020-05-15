package jean.wencelius.traceurrecopem.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputBoat extends AppCompatActivity {

    public String mBoat;
    public String mBoatOwner;

    private long trackId;
    private boolean mNewPicAdded;

    private Button mButton;
    private CheckBox mCheckBox;
    private RadioButton radioMotorboat;
    private RadioButton radioOutrigger;
    private RadioButton radioSwim;
    private RadioButton radioShore;

    public static final String BUNDLE_STATE_BOAT = "boatType";
    public static final String BUNDLE_STATE_BOAT_OWNER = "boatOwner";
    public static final String BUNDLE_STATE_BUTTON = "nxtButton";
    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_boat);

        mButton = (Button) findViewById(R.id.activity_data_input_boat_next_btn);
        mCheckBox = (CheckBox) findViewById(R.id.activity_data_input_boat_boat_owner);
        radioMotorboat = (RadioButton) findViewById(R.id.activity_data_input_boat_motor);
        radioOutrigger = (RadioButton) findViewById(R.id.activity_data_input_boat_pirogue);
        radioSwim = (RadioButton) findViewById(R.id.activity_data_input_boat_nage);
        radioShore = (RadioButton) findViewById(R.id.activity_data_input_boat_shore);

        if(savedInstanceState!=null){
            mButton.setEnabled(savedInstanceState.getBoolean(BUNDLE_STATE_BUTTON));
            mBoat = savedInstanceState.getString(BUNDLE_STATE_BOAT);
            mBoatOwner = savedInstanceState.getString(BUNDLE_STATE_BOAT_OWNER);
            trackId = savedInstanceState.getLong(BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            String boat = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT,getApplicationContext());
            String boatOwner = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT_OWNER,getApplicationContext());

            if(null != boat){
                mBoat = boat;
                mBoatOwner = boatOwner;
            }else{
                mBoat = "empty";
                mBoatOwner = "NA";
            }
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
        }

        //if(true){
        if(mBoat.equals("empty")){
            mButton.setEnabled(false);
        }else if(mBoat.equals("motorboat") || mBoat.equals("outrigger")){
            radioMotorboat.setChecked(mBoat.equals("motorboat"));
            radioOutrigger.setChecked(mBoat.equals("outrigger"));
            mCheckBox.setVisibility((View.VISIBLE));
            mCheckBox.setChecked(mBoatOwner.equals("true"));
            mButton.setEnabled(true);
        }else{
            radioShore.setChecked(mBoat.equals("from_shore"));
            radioSwim.setChecked(mBoat.equals("swim"));
            mCheckBox.setVisibility(View.INVISIBLE);
            mButton.setEnabled(true);
        }

        //TODO:
        setTitle("Question 2/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues boatValues = new ContentValues();
                boatValues.put(TrackContentProvider.Schema.COL_BOAT,mBoat);
                boatValues.put(TrackContentProvider.Schema.COL_BOAT_OWNER,mBoatOwner);

                getContentResolver().update(trackUri, boatValues, null, null);

                String textToDisplay ="Boat Type = " + mBoat + "\n" +
                        "Boat Owner = " +  mBoatOwner;

                Toast.makeText(dataInputBoat.this, textToDisplay, Toast.LENGTH_SHORT).show();

                Intent NextIntent = new Intent(dataInputBoat.this, dataInputCrew.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                startActivity(NextIntent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_BOAT,mBoat);
        outState.putString(BUNDLE_STATE_BOAT_OWNER,mBoatOwner);
        outState.putBoolean(BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);

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
        mButton.setEnabled(true);
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
}
