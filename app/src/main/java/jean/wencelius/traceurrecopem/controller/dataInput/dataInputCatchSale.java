package jean.wencelius.traceurrecopem.controller.dataInput;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackListActivity;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputCatchSale extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private String mCatchSaleAns;
    private int mCatchSaleN;
    private String mCatchSaleType;
    private int mCatchSaleTypeInt;
    private String mCatchSalePrice;
    private int mCatchSalePriceInt;
    private String mCatchSaleWhere;
    private int mCatchSaleWhereInt;
    private String mCatchSaleDetails;
    private String mCatchSalePicAns;

    private String mCatchDestination;
    private long trackId;
    private boolean mNewPicAdded;

    public static final String BUNDLE_STATE_ANS = "mainAnswer";
    public static final String BUNDLE_STATE_CATCH_N = "catchN";
    public static final String BUNDLE_STATE_TYPE_INT = "typeInt";
    public static final String BUNDLE_STATE_PRICE_INT = "priceInt";
    public static final String BUNDLE_STATE_WHERE_INT = "whereInt";
    public static final String BUNDLE_STATE_DETAILS = "details";
    public static final String BUNDLE_STATE_PIC_ANS = "picAnswer";
    public static final String BUNDLE_STATE_BUTTON = "nxtButton";
    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";
    public static final String BUNDLE_EXTRA_CATCH_DESTINATION = "catchDestination";
    public static final String BUNDLE_EXTRA_REPORTED_PIC ="reportedPic";

    //Views
    private Button mButton;
    private RelativeLayout mCatchSaleQuantityFrame;
    private LinearLayout mCatchSalePicFrame;

    private RadioButton mCatchSaleInputAnsY;
    private RadioButton mCatchSaleInputAnsN;
    private RadioButton mCatchSaleInputPicAnsY;
    private RadioButton mCatchSaleInputPicAnsN;

    private NumberPicker mCatchSaleInputN;
    private NumberPicker mCatchSaleInputType;
    private Spinner mCatchSaleInputPrice;
    private Spinner mCatchSaleInputWhere;
    private EditText mCatchSaleInputDetails;

    private String [] places;
    private String[] prices;
    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean priceValid;
    private boolean whereValid;
    private boolean picValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_sale);

        mButton = (Button) findViewById(R.id.activity_data_input_catch_sale_next_btn);

        mCatchSaleQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_sale_quantity_frame);
        mCatchSalePicFrame = (LinearLayout) findViewById(R.id.activity_catch_sale_pic_frame);

        mCatchSaleInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_yes);
        mCatchSaleInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_no);
        mCatchSaleInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_pic_yes);
        mCatchSaleInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_pic_no);

        mCatchSaleInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_N);
        mCatchSaleInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_type);
        mCatchSaleInputPrice = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_price);
        mCatchSaleInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_where);
        mCatchSaleInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_sale_input_details);

        //TODO: Modify
        mCatchDestination = "sale";

        mCatchSaleInputN.setMinValue(0);
        mCatchSaleInputN.setMaxValue(100);
        mCatchSaleInputN.setOnValueChangedListener(new nPicker());
        mCatchSaleInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchSaleInputType.setMinValue(0);
        mCatchSaleInputType.setMaxValue(type.length-1);
        mCatchSaleInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchSaleInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchSaleInputType.setDisplayedValues(type);
        mCatchSaleInputType.setOnValueChangedListener(new typePicker());

        prices = this.getResources().getStringArray(R.array.data_input_catch_sale_price);
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this, R.array.data_input_catch_sale_price,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchSaleInputPrice.setAdapter(priceAdapter);
        mCatchSaleInputPrice.setOnItemSelectedListener(this);

        places = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_input_catch_sale_where, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchSaleInputWhere.setAdapter(whereAdapter);
        mCatchSaleInputWhere.setOnItemSelectedListener(this);

        if(savedInstanceState != null){
            mButton.setEnabled(savedInstanceState.getBoolean(BUNDLE_STATE_BUTTON));

            mCatchSaleAns = savedInstanceState.getString(BUNDLE_STATE_ANS);
            mCatchSaleN = savedInstanceState.getInt(BUNDLE_STATE_CATCH_N);
            mCatchSaleTypeInt = savedInstanceState.getInt(BUNDLE_STATE_TYPE_INT);
            mCatchSaleType = type[mCatchSaleTypeInt];
            mCatchSalePriceInt = savedInstanceState.getInt(BUNDLE_STATE_PRICE_INT);
            mCatchSalePrice = prices[mCatchSalePriceInt];
            mCatchSaleWhereInt = savedInstanceState.getInt(BUNDLE_STATE_WHERE_INT);
            mCatchSaleWhere = places[mCatchSaleWhereInt];
            mCatchSalePicAns = savedInstanceState.getString(BUNDLE_STATE_PIC_ANS);
            mCatchSaleDetails = savedInstanceState.getString(BUNDLE_STATE_DETAILS);

            mCatchSaleInputAnsY.setSelected(mCatchSaleAns.equals("true"));
            mCatchSaleInputAnsN.setSelected(mCatchSaleAns.equals("false"));
            mCatchSaleInputPicAnsY.setSelected(mCatchSalePicAns.equals("true"));
            mCatchSaleInputPicAnsN.setSelected(mCatchSalePicAns.equals("false"));

            mCatchSaleInputN.setValue(mCatchSaleN);
            mCatchSaleInputType.setValue(mCatchSaleTypeInt);
            mCatchSaleInputPrice.setSelection(mCatchSalePriceInt);
            mCatchSaleInputWhere.setSelection(mCatchSaleWhereInt);
            mCatchSaleInputDetails.setText(mCatchSaleDetails);
            mCatchSaleInputDetails.setSelection(mCatchSaleDetails.length());

            trackId = savedInstanceState.getLong(BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            mButton.setEnabled(false);

            mCatchSaleAns = "empty";
            mCatchSaleN = 0;
            mCatchSaleType = "NA";
            mCatchSaleTypeInt = 0;
            mCatchSalePrice = "NA";
            mCatchSalePriceInt = 0;
            mCatchSalePicAns = "NA";
            mCatchSaleDetails = "NA";

            String catchSaleWhere = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF,getApplicationContext());
            if(null!=catchSaleWhere){
                mCatchSaleWhere = catchSaleWhere;
                mCatchSaleWhereInt = Arrays.asList(places).indexOf(catchSaleWhere);
                mCatchSaleInputWhere.setSelection(mCatchSaleWhereInt);
            }else{
                mCatchSaleWhere = "NA";
                mCatchSaleWhereInt = 0;
            }
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
        }

        if(mCatchSaleAns.equals("true")){
            mCatchSaleQuantityFrame.setVisibility(View.VISIBLE);
            mCatchSalePicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchSaleQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchSalePicFrame.setVisibility(View.INVISIBLE);
        }

        nValid = mCatchSaleN!=0;
        typeValid = mCatchSaleTypeInt!=0;
        priceValid = mCatchSalePriceInt!=0;
        whereValid = mCatchSaleWhereInt!=0;
        picValid = mCatchSalePicAns.equals("true") || mCatchSalePicAns.equals("false");

        //TODO:
        setTitle("Question 5/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCatchSaleDetails = mCatchSaleInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchSaleValues = new ContentValues();
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE,mCatchSaleAns);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_N,mCatchSaleN);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_TYPE,mCatchSaleType);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_PRICE,mCatchSalePrice);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_WHERE,mCatchSaleWhere);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS,mCatchSaleDetails);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_SALE_PIC,mCatchSalePicAns);

                getContentResolver().update(trackUri, catchSaleValues, null, null);

                String textToDisplay ="Sold Catch = " + mCatchSaleAns + "\n" +
                        "Sold N = " +  mCatchSaleN + " - " + mCatchSaleType +"\n" +
                        "Price = " + mCatchSalePrice +"\n" +
                        "Sold in = "+  mCatchSaleWhere +"\n" +
                        "Details = " + mCatchSaleDetails +"\n" +
                        "Pictures = " + mCatchSalePicAns;

                Toast.makeText(dataInputCatchSale.this, textToDisplay, Toast.LENGTH_LONG).show();

                Intent NextIntent = new Intent(dataInputCatchSale.this, TrackListActivity.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                NextIntent.putExtra(BUNDLE_EXTRA_REPORTED_PIC, mCatchSalePicAns);
                startActivity(NextIntent);
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_sale_question_no:
                if (checked) {
                    mCatchSaleAns="false";
                    mButton.setEnabled(true);
                    mCatchSaleQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchSalePicFrame.setVisibility(View.INVISIBLE);
                    mCatchSaleN = 0;
                    mCatchSaleType = "NA";
                    mCatchSaleTypeInt = 0;
                    mCatchSalePrice = "NA";
                    mCatchSalePriceInt = 0;
                    mCatchSaleWhere = "NA";
                    mCatchSaleWhereInt = 0;
                    mCatchSalePicAns = "NA";
                }
                break;
            case R.id.activity_data_input_catch_sale_question_yes:
                if (checked) {
                    mCatchSaleAns="true";
                    mButton.setEnabled(false);
                    mCatchSaleQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchSalePicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_sale_question_pic_no:
                if (checked) {
                    mCatchSalePicAns = "false";
                    picValid=true;
                    if(nValid && typeValid && priceValid && whereValid && picValid)
                        mButton.setEnabled(true);
                    else
                        mButton.setEnabled(false);
                    Intent fishCaughtIntent = new Intent(dataInputCatchSale.this, dataInputFishCaught.class);
                    fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                    fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                    fishCaughtIntent.putExtra(BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
                    fishCaughtIntent.putExtra(BUNDLE_EXTRA_REPORTED_PIC,mCatchSalePicAns);
                    startActivity(fishCaughtIntent);
                }
                break;
            case R.id.activity_data_input_catch_sale_question_pic_yes:
                if (checked) {
                    mCatchSalePicAns = "true";
                    picValid=true;
                    if(nValid && typeValid && priceValid && whereValid && picValid)
                        mButton.setEnabled(true);
                    else
                        mButton.setEnabled(false);
                }
                break;
        }
    }

   @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;

        if(spin.getId() == R.id.activity_data_input_catch_sale_input_price)
        {
            mCatchSalePrice = prices[position];
            mCatchSalePriceInt=position;
            if(mCatchSalePrice.equals("Prix pour un"))
                priceValid = false;
            else
                priceValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }else{
            mCatchSaleWhere = places[position];
            mCatchSaleWhereInt=position;
            if(mCatchSaleWhere.equals("Choisi le lieu"))
                whereValid = false;
            else
                whereValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mButton.setEnabled(false);
    }

    class nPicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchSaleN = newVal;
            if(mCatchSaleN==0)
                nValid = false;
            else
                nValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);

        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchSaleType = type[newVal];
            mCatchSaleTypeInt = newVal;
            if(mCatchSaleType.equals("Choisi"))
                typeValid = false;
            else
                typeValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString(BUNDLE_STATE_ANS,mCatchSaleAns);
        outState.putInt(BUNDLE_STATE_CATCH_N,mCatchSaleN);
        outState.putInt(BUNDLE_STATE_TYPE_INT,mCatchSaleTypeInt);
        outState.putInt(BUNDLE_STATE_PRICE_INT,mCatchSalePriceInt);
        outState.putInt(BUNDLE_STATE_WHERE_INT,mCatchSaleWhereInt);
        outState.putString(BUNDLE_STATE_DETAILS,mCatchSaleInputDetails.getText().toString());
        outState.putString(BUNDLE_STATE_PIC_ANS,mCatchSalePicAns);
        outState.putBoolean(BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        super.onSaveInstanceState(outState);
    }
}
