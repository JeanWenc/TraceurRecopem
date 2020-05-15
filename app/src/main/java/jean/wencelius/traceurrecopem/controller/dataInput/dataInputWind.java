package jean.wencelius.traceurrecopem.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;


public class dataInputWind extends AppCompatActivity {

    public String mWindEstFisher;
    public String mCurrentEstFisher;

    private long trackId;
    private boolean mNewPicAdded;

    private Button mButton;

    private Boolean cg1;
    private Boolean cg2;

    public static final String BUNDLE_STATE_BUTTON = "nxtButton";
    public static final String BUNDLE_STATE_WIND = "wind";
    public static final String BUNDLE_STATE_CURRENT = "current";
    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_wind);

        mButton = (Button) findViewById(R.id.activity_data_input_wind_next_btn);

        if(savedInstanceState!=null){
            mButton.setEnabled(savedInstanceState.getBoolean(BUNDLE_STATE_BUTTON));
            mWindEstFisher = savedInstanceState.getString(BUNDLE_STATE_WIND);
            mCurrentEstFisher = savedInstanceState.getString(BUNDLE_STATE_CURRENT);

            cg1 = !mWindEstFisher.equals("empty");
            cg2 = !mCurrentEstFisher.equals("empty");

            trackId = savedInstanceState.getLong(BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            mButton.setEnabled(false);

            cg1 = false;
            cg2 = false;

            mWindEstFisher = "empty";
            mCurrentEstFisher = "empty";

            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
        }

        //TODO:
        setTitle("Question 4/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues windValues = new ContentValues();
                windValues.put(TrackContentProvider.Schema.COL_WIND_FISHER,mWindEstFisher);
                windValues.put(TrackContentProvider.Schema.COL_CURRENT_FISHER,mCurrentEstFisher);

                getContentResolver().update(trackUri, windValues, null, null);

                String textToDisplay ="Wind = " + mWindEstFisher + "\n" +
                        "Current = " +  mCurrentEstFisher+ "\n";

                Toast.makeText(dataInputWind.this, textToDisplay, Toast.LENGTH_SHORT).show();

                Intent NextIntent = new Intent(dataInputWind.this, dataInputCatchSale.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                startActivity(NextIntent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_WIND,mWindEstFisher);
        outState.putString(BUNDLE_STATE_CURRENT,mCurrentEstFisher);
        outState.putBoolean(BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
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

        if(cg1 && cg2)
            mButton.setEnabled(true);
    }
}
