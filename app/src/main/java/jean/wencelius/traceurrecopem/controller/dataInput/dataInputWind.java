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
import jean.wencelius.traceurrecopem.controller.TrackListActivity;


public class dataInputWind extends AppCompatActivity {

    public String mWindEstFisher;

    public String mCurrentEstFisher;

    private Button mButton;

    private Boolean cg1;
    private Boolean cg2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_wind);

        mButton = (Button) findViewById(R.id.activity_data_input_wind_next_btn);
        mButton.setEnabled(false);

        cg1 = false;
        cg2=false;

        mWindEstFisher = "empty";

        //TODO:
        setTitle("Question 4/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent NextIntent = new Intent(dataInputWind.this, dataInputCatchSale.class);
                startActivity(NextIntent);
            }
        });
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

        Toast.makeText(this, mWindEstFisher + mCurrentEstFisher, Toast.LENGTH_SHORT).show();

        if(cg1 && cg2)
            mButton.setEnabled(true);
    }
}
