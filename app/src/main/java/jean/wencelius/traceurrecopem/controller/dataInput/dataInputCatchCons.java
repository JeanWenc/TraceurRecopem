package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackListActivity;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputCatchCons extends AppCompatActivity{

    private String mCatchConsAns;
    private int mCatchConsN;
    private String mCatchConsType;
    private int mCatchConsTypeInt;
    private String mCatchConsDetails;
    private String mCatchConsPicAns;

    private String mCatchSalePicAns;
    private String mCatchOrderPicAns;
    private String mCatchGivePicAns;

    private String mCatchDestination;
    private long trackId;
    private boolean mNewPicAdded;

    //Views
    private Button mButton;
    private TextView mPicConsQuestion;
    private RelativeLayout mCatchConsQuantityFrame;
    private LinearLayout mCatchConsPicFrame;

    private RadioButton mCatchConsInputAnsY;
    private RadioButton mCatchConsInputAnsN;
    private RadioButton mCatchConsInputPicAnsY;
    private RadioButton mCatchConsInputPicAnsN;

    private NumberPicker mCatchConsInputN;
    private NumberPicker mCatchConsInputType;
    private EditText mCatchConsInputDetails;

    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean picValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_cons);

        mButton = (Button) findViewById(R.id.activity_data_input_catch_cons_next_btn);

        mPicConsQuestion = (TextView) findViewById(R.id.activity_data_input_catch_cons_question_pic);

        mCatchConsQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_cons_quantity_frame);
        mCatchConsPicFrame = (LinearLayout) findViewById(R.id.activity_catch_cons_pic_frame);

        mCatchConsInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_yes);
        mCatchConsInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_no);
        mCatchConsInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_pic_yes);
        mCatchConsInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_cons_question_pic_no);

        mCatchConsInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_cons_input_N);
        mCatchConsInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_cons_input_type);
        mCatchConsInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_cons_input_details);

        mCatchDestination = "cons";

        mCatchConsInputN.setMinValue(0);
        mCatchConsInputN.setMaxValue(100);
        mCatchConsInputN.setOnValueChangedListener(new dataInputCatchCons.nPicker());
        mCatchConsInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchConsInputType.setMinValue(0);
        mCatchConsInputType.setMaxValue(type.length-1);
        mCatchConsInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchConsInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchConsInputType.setDisplayedValues(type);
        mCatchConsInputType.setOnValueChangedListener(new dataInputCatchCons.typePicker());

        if(savedInstanceState != null){
            mButton.setEnabled(savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON));

            mCatchConsAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);
            mCatchConsN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchConsTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchConsType = type[mCatchConsTypeInt];
            mCatchConsPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_PIC_ANS);
            mCatchConsDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);

            mCatchSalePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);
            mCatchOrderPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);
            mCatchGivePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS);

            mCatchConsInputAnsY.setSelected(mCatchConsAns.equals("true"));
            mCatchConsInputAnsN.setSelected(mCatchConsAns.equals("false"));
            mCatchConsInputPicAnsY.setSelected(mCatchConsPicAns.equals("true"));
            mCatchConsInputPicAnsN.setSelected(mCatchConsPicAns.equals("false"));

            mCatchConsInputN.setValue(mCatchConsN);
            mCatchConsInputType.setValue(mCatchConsTypeInt);
            mCatchConsInputDetails.setText(mCatchConsDetails);
            mCatchConsInputDetails.setSelection(mCatchConsDetails.length());

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            mButton.setEnabled(false);

            mCatchConsAns = "empty";
            mCatchConsN = 0;
            mCatchConsType = "NA";
            mCatchConsTypeInt = 0;
            mCatchConsPicAns = "NA";
            mCatchConsDetails = "NA";

            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);

            mCatchSalePicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);
            mCatchOrderPicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);
            mCatchGivePicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS);
        }

        if(mCatchConsAns.equals("true")){
            mCatchConsQuantityFrame.setVisibility(View.VISIBLE);
            mCatchConsPicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchConsQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchConsPicFrame.setVisibility(View.INVISIBLE);
        }

        if(mCatchSalePicAns.equals("true") || mCatchOrderPicAns.equals("true") || mCatchGivePicAns.equals("true")) mPicConsQuestion.setText(R.string.data_input_catch_order_question_pic_if_sale_pic);

        nValid = mCatchConsN!=0;
        typeValid = mCatchConsTypeInt!=0;
        picValid = mCatchConsPicAns.equals("true") || mCatchConsPicAns.equals("false");

        setTitle("Question 8/8");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCatchConsDetails = mCatchConsInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchSaleValues = new ContentValues();
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_CONS,mCatchConsAns);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_N,mCatchConsN);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_TYPE,mCatchConsType);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_DETAILS,mCatchConsDetails);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_CONS_PIC,mCatchConsPicAns);

                getContentResolver().update(trackUri, catchSaleValues, null, null);

                String textToDisplay ="Sold Cons Catch = " + mCatchConsAns + "\n" +
                        "Sold N = " +  mCatchConsN + " - " + mCatchConsType +"\n" +
                        "Details = " + mCatchConsDetails +"\n" +
                        "Pictures = " + mCatchConsPicAns;

                Toast.makeText(dataInputCatchCons.this, textToDisplay, Toast.LENGTH_LONG).show();

                Intent NextIntent = new Intent(dataInputCatchCons.this, TrackListActivity.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);

                NextIntent.putExtra(recopemValues.BUNDLE_STATE_SALE_PIC_ANS, mCatchSalePicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS,mCatchGivePicAns);
                startActivity(NextIntent);
                finish();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_cons_question_no:
                if (checked) {
                    mCatchConsAns="false";
                    mButton.setEnabled(true);
                    mCatchConsQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchConsPicFrame.setVisibility(View.INVISIBLE);
                    mCatchConsN = 0;
                    mCatchConsType = "NA";
                    mCatchConsTypeInt = 0;
                    mCatchConsPicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_cons_question_yes:
                if (checked) {
                    mCatchConsAns="true";
                    mButton.setEnabled(false);
                    mCatchConsQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchConsPicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_cons_question_pic_no:
                if (checked) {
                    mCatchConsPicAns = "false";
                    picValid=true;
                    if(nValid && typeValid && picValid)
                        mButton.setEnabled(true);
                    else
                        mButton.setEnabled(false);
                    LaunchFishCaughtIntent();
                }
                break;
            case R.id.activity_data_input_catch_cons_question_pic_yes:
                if (checked) {
                    mCatchConsPicAns = "true";
                    picValid=true;
                    if(nValid && typeValid && picValid)
                        mButton.setEnabled(true);
                    else
                        mButton.setEnabled(false);

                    if(mCatchSalePicAns.equals("true") || mCatchOrderPicAns.equals("true") || mCatchGivePicAns.equals("true")) LaunchFishCaughtIntent();
                }
                break;
        }
    }

    class nPicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchConsN = newVal;
            if(mCatchConsN==0)
                nValid = false;
            else
                nValid = true;

            if(nValid && typeValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchConsType = type[newVal];
            mCatchConsTypeInt = newVal;
            if(mCatchConsType.equals("Choisi"))
                typeValid = false;
            else
                typeValid = true;

            if(nValid && typeValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(recopemValues.BUNDLE_STATE_ANS,mCatchConsAns);
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchConsN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchConsTypeInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchConsInputDetails.getText().toString());
        outState.putString(recopemValues.BUNDLE_STATE_PIC_ANS,mCatchConsPicAns);
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);

        outState.putString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS,mCatchSalePicAns);
        outState.putString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
        outState.putString(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS,mCatchGivePicAns);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchCons.this, dataInputFishCaught.class);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
        fishCaughtIntent.putExtra(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        startActivity(fishCaughtIntent);
    }
}