package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackListActivity;

public class dataInputCrew extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private String mCrewAlone;
    private int mCrewN;
    private String mCrewWho;

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
        mButton.setEnabled(false);

        mCrewQuestionN = (TextView) findViewById(R.id.activity_data_input_crew_question_N);
        mCrewQuestionWho = (TextView) findViewById(R.id.activity_data_input_crew_question_who);
        mCrewQuestionWhoDetails = (TextView) findViewById(R.id.activity_data_input_crew_question_who_details);
        mCrewInputWho = (EditText) findViewById(R.id.activity_data_input_crew_input_who);


        mCrewInputN = (NumberPicker) findViewById(R.id.activity_data_input_crew_input_N);

        mCrewQuestionN.setVisibility(View.INVISIBLE);
        mCrewQuestionWho.setVisibility(View.INVISIBLE);
        mCrewQuestionWhoDetails.setVisibility(View.INVISIBLE);
        mCrewInputWho.setVisibility(View.INVISIBLE);
        mCrewInputN.setVisibility(View.INVISIBLE);

        mCrewInputN.setMinValue(0);
        mCrewInputN.setMaxValue(10);
        mCrewInputN.setOnValueChangedListener(this);

        mCrewAlone = "empty";
        mCrewN=0;
        mCrewWho = "NA";

        //TODO:
        setTitle("Question 3/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCrewAlone.equals("true")){
                    //don't fill in mCrewN and mCrewWho
                }else{
                    //fill in mCrewN
                    String crewWho = mCrewInputWho.getText().toString();
                    mCrewWho =  crewWho;
                }

                Toast.makeText(dataInputCrew.this, Integer.toString(mCrewN) + mCrewWho, Toast.LENGTH_SHORT).show();

                Intent NextIntent = new Intent(dataInputCrew.this, dataInputWind.class);
                startActivity(NextIntent);
            }
        });
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
                    mButton.setEnabled(false);
                    mCrewQuestionN.setVisibility(View.VISIBLE);
                    mCrewQuestionWho.setVisibility(View.VISIBLE);
                    mCrewQuestionWhoDetails.setVisibility(View.VISIBLE);
                    mCrewInputWho.setVisibility(View.VISIBLE);
                    mCrewInputN.setVisibility(View.VISIBLE);
                }
                break;
        }
        Toast.makeText(this, mCrewAlone, Toast.LENGTH_SHORT).show();
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
