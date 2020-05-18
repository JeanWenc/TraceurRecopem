package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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

public class dataInputCatchGive extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static dataInputCatchGive giveAct;

    private ContentResolver mCr;
    private Cursor mTrackCursor;

    private String mCatchGiveAns;
    private int mCatchGiveN;
    private String mCatchGiveType;
    private int mCatchGiveTypeInt;
    private String mCatchGiveWhere;
    private int mCatchGiveWhereInt;
    private String mCatchGiveDetails;
    private String mCatchGivePicAns;

    private String mCatchSalePicAns;
    private String mCatchOrderPicAns;

    private String mCatchDestination;
    private long trackId;
    private boolean mNewPicAdded;

    //Views
    private Button mButton;
    private TextView mPicGiveQuestion;
    private RelativeLayout mCatchGiveQuantityFrame;
    private LinearLayout mCatchGivePicFrame;

    private RadioButton mCatchGiveInputAnsY;
    private RadioButton mCatchGiveInputAnsN;
    private RadioButton mCatchGiveInputPicAnsY;
    private RadioButton mCatchGiveInputPicAnsN;

    private NumberPicker mCatchGiveInputN;
    private NumberPicker mCatchGiveInputType;
    private Spinner mCatchGiveInputWhere;
    private EditText mCatchGiveInputDetails;

    private String [] places;
    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean whereValid;
    private boolean picValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_give);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        giveAct = this;

        mButton = (Button) findViewById(R.id.activity_data_input_catch_give_next_btn);

        mPicGiveQuestion = (TextView) findViewById(R.id.activity_data_input_catch_give_question_pic);

        mCatchGiveQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_give_quantity_frame);
        mCatchGivePicFrame = (LinearLayout) findViewById(R.id.activity_catch_give_pic_frame);

        mCatchGiveInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_yes);
        mCatchGiveInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_no);
        mCatchGiveInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_pic_yes);
        mCatchGiveInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_give_question_pic_no);

        mCatchGiveInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_give_input_N);
        mCatchGiveInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_give_input_type);
        mCatchGiveInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_give_input_where);
        mCatchGiveInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_give_input_details);

        mCatchDestination = "give";

        mCatchGiveInputN.setMinValue(0);
        mCatchGiveInputN.setMaxValue(100);
        mCatchGiveInputN.setOnValueChangedListener(new dataInputCatchGive.nPicker());
        mCatchGiveInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchGiveInputType.setMinValue(0);
        mCatchGiveInputType.setMaxValue(type.length-1);
        mCatchGiveInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchGiveInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchGiveInputType.setDisplayedValues(type);
        mCatchGiveInputType.setOnValueChangedListener(new dataInputCatchGive.typePicker());

        places = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_input_catch_sale_where, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchGiveInputWhere.setAdapter(whereAdapter);
        mCatchGiveInputWhere.setOnItemSelectedListener(this);

        if(savedInstanceState != null){
            mCatchGiveAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);
            mCatchGiveN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchGiveTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchGiveType = type[mCatchGiveTypeInt];
            mCatchGiveWhereInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_WHERE_INT);
            mCatchGiveWhere = places[mCatchGiveWhereInt];
            mCatchGivePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_PIC_ANS);
            mCatchGiveDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);

            mCatchSalePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);
            mCatchOrderPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);

            mCatchSalePicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);
            mCatchOrderPicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS);

            mCr = getContentResolver();
            mTrackCursor = mCr.query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String catchGiveAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE));
            int catchGiveN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_N));
            String catchGiveType = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_TYPE));
            String catchGiveWhere = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_WHERE));
            String catchGiveDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_DETAILS));
            String catchGivePicAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_PIC));

            mTrackCursor.close();
            if(catchGiveAns!=null){
                mCatchGiveAns = catchGiveAns;
                mCatchGiveN = catchGiveN;
                mCatchGiveType = catchGiveType;
                if (catchGiveType!=null){
                    mCatchGiveTypeInt = Arrays.asList(type).indexOf(catchGiveType);
                }else{
                    mCatchGiveTypeInt = 0;
                }
                mCatchGiveWhere = catchGiveWhere;
                if (catchGiveWhere!=null){
                    mCatchGiveWhereInt = Arrays.asList(places).indexOf(catchGiveWhere);
                }else{
                    mCatchGiveWhereInt = 0;
                }
                mCatchGivePicAns = catchGivePicAns;
                mCatchGiveDetails = catchGiveDetails;
            }else {
                mCatchGiveAns = "empty";
                mCatchGiveN = 0;
                mCatchGiveType = type[0];
                mCatchGiveTypeInt = 0;
                mCatchGivePicAns = "NA";
                mCatchGiveDetails = "NA";

                String catchSaleWhere = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF, getApplicationContext());
                if (null != catchSaleWhere) {
                    mCatchGiveWhere = catchSaleWhere;
                    mCatchGiveWhereInt = Arrays.asList(places).indexOf(catchSaleWhere);
                    mCatchGiveInputWhere.setSelection(mCatchGiveWhereInt);
                } else {
                    mCatchGiveWhere = places[0];
                    mCatchGiveWhereInt = 0;
                }
            }
        }

        mCatchGiveInputAnsY.setChecked(mCatchGiveAns.equals("true"));
        mCatchGiveInputAnsN.setChecked(mCatchGiveAns.equals("false"));
        mCatchGiveInputPicAnsY.setChecked(mCatchGivePicAns.equals("true"));
        mCatchGiveInputPicAnsN.setChecked(mCatchGivePicAns.equals("false"));

        mCatchGiveInputN.setValue(mCatchGiveN);
        mCatchGiveInputType.setValue(mCatchGiveTypeInt);
        mCatchGiveInputWhere.setSelection(mCatchGiveWhereInt);

        if(!mCatchGiveDetails.equals("NA")){
            mCatchGiveInputDetails.setText(mCatchGiveDetails);
            mCatchGiveInputDetails.setSelection(mCatchGiveDetails.length());
        }

        if(mCatchGiveAns.equals("true")){
            mCatchGiveQuantityFrame.setVisibility(View.VISIBLE);
            mCatchGivePicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchGiveQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchGivePicFrame.setVisibility(View.INVISIBLE);
        }

        if(mCatchSalePicAns.equals("true") || mCatchOrderPicAns.equals("true")) mPicGiveQuestion.setText(R.string.data_input_catch_give_question_pic_if_sale_pic);

        nValid = mCatchGiveN!=0;
        typeValid = mCatchGiveTypeInt!=0;
        whereValid = mCatchGiveWhereInt!=0;
        picValid = mCatchGivePicAns.equals("true") || mCatchGivePicAns.equals("false");

        mButton.setEnabled(nValid && typeValid && whereValid && picValid);

        setTitle("Question 7/8");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCatchGiveDetails = mCatchGiveInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchGiveValues = new ContentValues();
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE,mCatchGiveAns);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_N,mCatchGiveN);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_TYPE,mCatchGiveType);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_WHERE,mCatchGiveWhere);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_DETAILS,mCatchGiveDetails);
                catchGiveValues.put(TrackContentProvider.Schema.COL_CATCH_GIVE_PIC,mCatchGivePicAns);

                getContentResolver().update(trackUri, catchGiveValues, null, null);

                String textToDisplay ="Sold Give Catch = " + mCatchGiveAns + "\n" +
                        "Sold N = " +  mCatchGiveN + " - " + mCatchGiveType +"\n" +
                        "Sold in = "+  mCatchGiveWhere +"\n" +
                        "Details = " + mCatchGiveDetails +"\n" +
                        "Pictures = " + mCatchGivePicAns;

                Toast.makeText(dataInputCatchGive.this, textToDisplay, Toast.LENGTH_LONG).show();

                Intent NextIntent = new Intent(dataInputCatchGive.this, dataInputCatchCons.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);

                NextIntent.putExtra(recopemValues.BUNDLE_STATE_SALE_PIC_ANS, mCatchSalePicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_GIVE_PIC_ANS,mCatchGivePicAns);
                startActivity(NextIntent);
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_give_question_no:
                if (checked) {
                    mCatchGiveAns="false";
                    mButton.setEnabled(true);
                    mCatchGiveQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchGivePicFrame.setVisibility(View.INVISIBLE);
                    mCatchGiveN = 0;
                    mCatchGiveType = type[0];
                    mCatchGiveTypeInt = 0;
                    mCatchGiveWhere = places[0];
                    mCatchGiveWhereInt = 0;
                    mCatchGivePicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_give_question_yes:
                if (checked) {
                    mCatchGiveAns="true";
                    mButton.setEnabled(false);
                    mCatchGiveQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchGivePicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_give_question_pic_no:
                if (checked) {
                    mCatchGivePicAns = "false";
                    picValid=true;
                    mButton.setEnabled(nValid && typeValid && whereValid && picValid);
                    LaunchFishCaughtIntent();
                }
                break;
            case R.id.activity_data_input_catch_give_question_pic_yes:
                if (checked) {
                    mCatchGivePicAns = "true";
                    picValid=true;
                    mButton.setEnabled(nValid && typeValid && whereValid && picValid);
                    if(mCatchSalePicAns.equals("true") || mCatchOrderPicAns.equals("true")) LaunchFishCaughtIntent();
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;
        mCatchGiveWhere = places[position];
        mCatchGiveWhereInt=position;
        whereValid = !mCatchGiveWhere.equals(places[0]);
        mButton.setEnabled(nValid && typeValid && whereValid && picValid);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mButton.setEnabled(false);
    }

    class nPicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchGiveN = newVal;
            nValid = mCatchGiveN!=0;
            mButton.setEnabled(nValid && typeValid && whereValid && picValid);
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchGiveType = type[newVal];
            mCatchGiveTypeInt = newVal;
            typeValid = !mCatchGiveType.equals(type[0]);
            mButton.setEnabled(nValid && typeValid && whereValid && picValid);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(recopemValues.BUNDLE_STATE_ANS,mCatchGiveAns);
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchGiveN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchGiveTypeInt);
        outState.putInt(recopemValues.BUNDLE_STATE_WHERE_INT,mCatchGiveWhereInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchGiveInputDetails.getText().toString());
        outState.putString(recopemValues.BUNDLE_STATE_PIC_ANS,mCatchGivePicAns);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);

        outState.putString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS,mCatchSalePicAns);
        outState.putString(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchGive.this, dataInputFishCaught.class);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
        fishCaughtIntent.putExtra(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        startActivity(fishCaughtIntent);
    }

    public static dataInputCatchGive getInstance(){
        return   giveAct;
    }
}
