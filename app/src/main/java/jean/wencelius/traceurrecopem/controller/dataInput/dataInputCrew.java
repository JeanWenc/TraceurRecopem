package jean.wencelius.traceurrecopem.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputCrew extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private String mCrewAlone;
    private int mCrewN;
    private String mCrewWho;

    private long trackId;
    private boolean mNewPicAdded;

    private static final String BUNDLE_STATE_CREW_ANS = "crewAns";
    private static final String BUNDLE_STATE_CREW_N = "crewN";
    private static final String BUNDLE_STATE_CREW_WHO = "crewWho";

    //Views
    private Button mButton;
    private TextView mCrewQuestionN;
    private NumberPicker mCrewInputN;
    private TextView mCrewQuestionWho;
    private TextView mCrewQuestionWhoDetails;
    private EditText mCrewInputWho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_crew);

        mButton = (Button) findViewById(R.id.activity_data_input_crew_next_btn);

        mCrewQuestionN = (TextView) findViewById(R.id.activity_data_input_crew_question_N);
        mCrewQuestionWho = (TextView) findViewById(R.id.activity_data_input_crew_question_who);
        mCrewQuestionWhoDetails = (TextView) findViewById(R.id.activity_data_input_crew_question_who_details);
        mCrewInputWho = (EditText) findViewById(R.id.activity_data_input_crew_input_who);

        mCrewInputN = (NumberPicker) findViewById(R.id.activity_data_input_crew_input_N);
        mCrewInputN.setMinValue(0);
        mCrewInputN.setMaxValue(10);
        mCrewInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCrewInputN.setOnValueChangedListener(this);

        if(savedInstanceState!=null){
            mButton.setEnabled(savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON));
            mCrewAlone = savedInstanceState.getString(BUNDLE_STATE_CREW_ANS);
            mCrewN = savedInstanceState.getInt(BUNDLE_STATE_CREW_N);
            mCrewWho = savedInstanceState.getString(BUNDLE_STATE_CREW_WHO);

            if(mCrewN!=0) mCrewInputN.setValue(mCrewN);
            if(mCrewWho!=""){
                mCrewInputWho.setText(mCrewWho);
                mCrewInputWho.setSelection(mCrewWho.length());
            }
            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            mCrewAlone = "empty";
            mCrewN=0;
            mCrewWho = "NA";

            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);

            mButton.setEnabled(false);
        }

        if(mCrewAlone.equals("false")){
            mCrewQuestionN.setVisibility(View.VISIBLE);
            mCrewQuestionWho.setVisibility(View.VISIBLE);
            mCrewQuestionWhoDetails.setVisibility(View.VISIBLE);
            mCrewInputWho.setVisibility(View.VISIBLE);
            mCrewInputN.setVisibility(View.VISIBLE);
        }else{
            mCrewQuestionN.setVisibility(View.INVISIBLE);
            mCrewQuestionWho.setVisibility(View.INVISIBLE);
            mCrewQuestionWhoDetails.setVisibility(View.INVISIBLE);
            mCrewInputWho.setVisibility(View.INVISIBLE);
            mCrewInputN.setVisibility(View.INVISIBLE);
        }

        //TODO:
        setTitle("Question 3/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String crewWho = mCrewInputWho.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues crewValues = new ContentValues();
                crewValues.put(TrackContentProvider.Schema.COL_CREW_ALONE,mCrewAlone);
                crewValues.put(TrackContentProvider.Schema.COL_CREW_N,mCrewN);
                crewValues.put(TrackContentProvider.Schema.COL_CREW_WHO,crewWho);

                getContentResolver().update(trackUri, crewValues, null, null);

                String textToDisplay ="Crew Alone = " + mCrewAlone + "\n" +
                        "Crew N = " +  mCrewN+ "\n" +
                        "Crew Who = " +  crewWho + "\n";

                Toast.makeText(dataInputCrew.this, textToDisplay, Toast.LENGTH_SHORT).show();

                Intent NextIntent = new Intent(dataInputCrew.this, dataInputWind.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                startActivity(NextIntent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_CREW_ANS,mCrewAlone);
        outState.putInt(BUNDLE_STATE_CREW_N,mCrewN);
        outState.putString(BUNDLE_STATE_CREW_WHO,mCrewInputWho.getText().toString());
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        super.onSaveInstanceState(outState);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_crew_question_yes:
                if (checked) {
                    mCrewAlone="true";
                    mButton.setEnabled(true);
                    mCrewQuestionN.setVisibility(View.INVISIBLE);
                    mCrewQuestionWho.setVisibility(View.INVISIBLE);
                    mCrewQuestionWhoDetails.setVisibility(View.INVISIBLE);
                    mCrewInputWho.setVisibility(View.INVISIBLE);
                    mCrewInputN.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.activity_data_input_crew_question_no:
                if (checked) {
                    mCrewAlone="false";
                    if(mCrewN!=0){
                        mButton.setEnabled(true);
                    }else{
                        mButton.setEnabled(false);
                    }
                    mCrewQuestionN.setVisibility(View.VISIBLE);
                    mCrewQuestionWho.setVisibility(View.VISIBLE);
                    mCrewQuestionWhoDetails.setVisibility(View.VISIBLE);
                    mCrewInputWho.setVisibility(View.VISIBLE);
                    mCrewInputN.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        mCrewN = newVal;
        if (newVal!=0)
            mButton.setEnabled(true);
        else
            mButton.setEnabled(false);
    }
}
