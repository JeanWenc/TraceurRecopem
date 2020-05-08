package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackDetailActivity;
import jean.wencelius.traceurrecopem.controller.TrackListActivity;

public class dataInputBoat extends AppCompatActivity {

    public String mBoat;

    public String mBoatOwner;

    private Button mButton;

    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_boat);

        mButton = (Button) findViewById(R.id.activity_data_input_boat_next_btn);
        mButton.setEnabled(false);

        mCheckBox = (CheckBox) findViewById(R.id.activity_data_input_boat_boat_owner);

        mCheckBox.setVisibility(View.INVISIBLE);

        mBoat = "empty";
        mBoatOwner = "NA";

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
