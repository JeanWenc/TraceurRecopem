package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackDetailActivity;
import jean.wencelius.traceurrecopem.controller.TrackListActivity;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputBoat extends AppCompatActivity {

    public String mBoat;
    public String mBoatOwner;

    private Button mButton;
    private CheckBox mCheckBox;
    private RadioButton radioMotorboat;
    private RadioButton radioOutrigger;
    private RadioButton radioSwim;
    private RadioButton radioShore;

    public static final String BUNDLE_STATE_BOAT = "boatType";
    public static final String BUNDLE_STATE_BOAT_OWNER = "boatOwner";
    public static final String BUNDLE_STATE_BUTTON = "nxtButton";

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

        }else{
            String boat = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT,getApplicationContext());
            String boatOwner = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT_OWNER,getApplicationContext());

            Toast.makeText(this, boat, Toast.LENGTH_SHORT).show();

            if(null != boat){
                mBoat = boat;
                mBoatOwner = boatOwner;
            }else{
                mBoat = "empty";
                mBoatOwner = "NA";
            }
        }

        if(true){
        //if(mBoat.equals("motorboat") || mBoat.equals("outrigger")){
            radioMotorboat.setChecked(mBoat.equals("motorboat"));
            radioOutrigger.setChecked(mBoat.equals("outrigger"));
            mCheckBox.setVisibility((View.VISIBLE));
            mCheckBox.setChecked(mBoatOwner.equals("true"));
            mButton.setEnabled(true);
        }else{
            radioShore.setChecked(mBoat.equals("from_shore"));
            radioSwim.setChecked(mBoat.equals("swim"));
            mCheckBox.setVisibility(View.INVISIBLE);
        }

        //TODO:
        setTitle("Question 2/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent NextIntent = new Intent(dataInputBoat.this, dataInputCrew.class);
                startActivity(NextIntent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_BOAT,mBoat);
        outState.putString(BUNDLE_STATE_BOAT_OWNER,mBoatOwner);
        outState.putBoolean(BUNDLE_STATE_BUTTON,mButton.isEnabled());
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
        //Toast.makeText(this, mBoatOwner, Toast.LENGTH_SHORT).show();
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
        //Toast.makeText(this, mBoatOwner, Toast.LENGTH_SHORT).show();
    }
}
