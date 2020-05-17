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

public class dataInputCatchOrder extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private String mCatchOrderAns;
    private int mCatchOrderN;
    private String mCatchOrderType;
    private int mCatchOrderTypeInt;
    private String mCatchOrderPrice;
    private int mCatchOrderPriceInt;
    private String mCatchOrderWhere;
    private int mCatchOrderWhereInt;
    private String mCatchOrderDetails;
    private String mCatchOrderPicAns;

    private String mCatchSalePicAns;

    private String mCatchDestination;
    private long trackId;
    private boolean mNewPicAdded;

    //Views
    private Button mButton;
    private TextView mPicOrderQuestion;
    private RelativeLayout mCatchOrderQuantityFrame;
    private LinearLayout mCatchOrderPicFrame;

    private RadioButton mCatchOrderInputAnsY;
    private RadioButton mCatchOrderInputAnsN;
    private RadioButton mCatchOrderInputPicAnsY;
    private RadioButton mCatchOrderInputPicAnsN;

    private NumberPicker mCatchOrderInputN;
    private NumberPicker mCatchOrderInputType;
    private Spinner mCatchOrderInputPrice;
    private Spinner mCatchOrderInputWhere;
    private EditText mCatchOrderInputDetails;

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
        setContentView(R.layout.activity_data_input_catch_order);

        mButton = (Button) findViewById(R.id.activity_data_input_catch_order_next_btn);

        mPicOrderQuestion = (TextView) findViewById(R.id.activity_data_input_catch_order_question_pic);

        mCatchOrderQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_order_quantity_frame);
        mCatchOrderPicFrame = (LinearLayout) findViewById(R.id.activity_catch_order_pic_frame);

        mCatchOrderInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_yes);
        mCatchOrderInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_no);
        mCatchOrderInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_pic_yes);
        mCatchOrderInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_pic_no);

        mCatchOrderInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_order_input_N);
        mCatchOrderInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_order_input_type);
        mCatchOrderInputPrice = (Spinner) findViewById(R.id.activity_data_input_catch_order_input_price);
        mCatchOrderInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_order_input_where);
        mCatchOrderInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_order_input_details);

        mCatchDestination = "order";

        mCatchOrderInputN.setMinValue(0);
        mCatchOrderInputN.setMaxValue(100);
        mCatchOrderInputN.setOnValueChangedListener(new dataInputCatchOrder.nPicker());
        mCatchOrderInputN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchOrderInputType.setMinValue(0);
        mCatchOrderInputType.setMaxValue(type.length-1);
        mCatchOrderInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mCatchOrderInputType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCatchOrderInputType.setDisplayedValues(type);
        mCatchOrderInputType.setOnValueChangedListener(new dataInputCatchOrder.typePicker());

        prices = this.getResources().getStringArray(R.array.data_input_catch_sale_price);
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter.createFromResource(this, R.array.data_input_catch_sale_price,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchOrderInputPrice.setAdapter(priceAdapter);
        mCatchOrderInputPrice.setOnItemSelectedListener(this);

        places = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_input_catch_sale_where, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCatchOrderInputWhere.setAdapter(whereAdapter);
        mCatchOrderInputWhere.setOnItemSelectedListener(this);

        if(savedInstanceState != null){
            mButton.setEnabled(savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON));

            mCatchOrderAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);
            mCatchOrderN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchOrderTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchOrderType = type[mCatchOrderTypeInt];
            mCatchOrderPriceInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_PRICE_INT);
            mCatchOrderPrice = prices[mCatchOrderPriceInt];
            mCatchOrderWhereInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_WHERE_INT);
            mCatchOrderWhere = places[mCatchOrderWhereInt];
            mCatchOrderPicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_PIC_ANS);
            mCatchOrderDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);

            mCatchSalePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);

            mCatchOrderInputAnsY.setSelected(mCatchOrderAns.equals("true"));
            mCatchOrderInputAnsN.setSelected(mCatchOrderAns.equals("false"));
            mCatchOrderInputPicAnsY.setSelected(mCatchOrderPicAns.equals("true"));
            mCatchOrderInputPicAnsN.setSelected(mCatchOrderPicAns.equals("false"));

            mCatchOrderInputN.setValue(mCatchOrderN);
            mCatchOrderInputType.setValue(mCatchOrderTypeInt);
            mCatchOrderInputPrice.setSelection(mCatchOrderPriceInt);
            mCatchOrderInputWhere.setSelection(mCatchOrderWhereInt);
            mCatchOrderInputDetails.setText(mCatchOrderDetails);
            mCatchOrderInputDetails.setSelection(mCatchOrderDetails.length());

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            mButton.setEnabled(false);

            mCatchOrderAns = "empty";
            mCatchOrderN = 0;
            mCatchOrderType = "NA";
            mCatchOrderTypeInt = 0;
            mCatchOrderPrice = "NA";
            mCatchOrderPriceInt = 0;
            mCatchOrderPicAns = "NA";
            mCatchOrderDetails = "NA";

            String catchSaleWhere = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF,getApplicationContext());
            if(null!=catchSaleWhere){
                mCatchOrderWhere = catchSaleWhere;
                mCatchOrderWhereInt = Arrays.asList(places).indexOf(catchSaleWhere);
                mCatchOrderInputWhere.setSelection(mCatchOrderWhereInt);
            }else{
                mCatchOrderWhere = "NA";
                mCatchOrderWhereInt = 0;
            }
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);

            mCatchSalePicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);
        }

        if(mCatchOrderAns.equals("true")){
            mCatchOrderQuantityFrame.setVisibility(View.VISIBLE);
            mCatchOrderPicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchOrderQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchOrderPicFrame.setVisibility(View.INVISIBLE);
        }

        if(mCatchSalePicAns.equals("true")) mPicOrderQuestion.setText(R.string.data_input_catch_order_question_pic_if_sale_pic);

        nValid = mCatchOrderN!=0;
        typeValid = mCatchOrderTypeInt!=0;
        priceValid = mCatchOrderPriceInt!=0;
        whereValid = mCatchOrderWhereInt!=0;
        picValid = mCatchOrderPicAns.equals("true") || mCatchOrderPicAns.equals("false");

        setTitle("Question 6/8");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCatchOrderDetails = mCatchOrderInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchSaleValues = new ContentValues();
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER,mCatchOrderAns);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_N,mCatchOrderN);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_TYPE,mCatchOrderType);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_PRICE,mCatchOrderPrice);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_WHERE,mCatchOrderWhere);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_DETAILS,mCatchOrderDetails);
                catchSaleValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_PIC,mCatchOrderPicAns);

                getContentResolver().update(trackUri, catchSaleValues, null, null);

                String textToDisplay ="Sold Order Catch = " + mCatchOrderAns + "\n" +
                        "Sold N = " +  mCatchOrderN + " - " + mCatchOrderType +"\n" +
                        "Price = " + mCatchOrderPrice +"\n" +
                        "Sold in = "+  mCatchOrderWhere +"\n" +
                        "Details = " + mCatchOrderDetails +"\n" +
                        "Pictures = " + mCatchOrderPicAns;

                Toast.makeText(dataInputCatchOrder.this, textToDisplay, Toast.LENGTH_LONG).show();

                Intent NextIntent = new Intent(dataInputCatchOrder.this, dataInputCatchGive.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);

                NextIntent.putExtra(recopemValues.BUNDLE_STATE_SALE_PIC_ANS, mCatchSalePicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
                startActivity(NextIntent);
                finish();
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_order_question_no:
                if (checked) {
                    mCatchOrderAns="false";
                    mButton.setEnabled(true);
                    mCatchOrderQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchOrderPicFrame.setVisibility(View.INVISIBLE);
                    mCatchOrderN = 0;
                    mCatchOrderType = "NA";
                    mCatchOrderTypeInt = 0;
                    mCatchOrderPrice = "NA";
                    mCatchOrderPriceInt = 0;
                    mCatchOrderWhere = "NA";
                    mCatchOrderWhereInt = 0;
                    mCatchOrderPicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_order_question_yes:
                if (checked) {
                    mCatchOrderAns="true";
                    mButton.setEnabled(false);
                    mCatchOrderQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchOrderPicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_order_question_pic_no:
                if (checked) {
                    mCatchOrderPicAns = "false";
                    picValid=true;
                    if(nValid && typeValid && priceValid && whereValid && picValid)
                        mButton.setEnabled(true);
                    else
                        mButton.setEnabled(false);
                    LaunchFishCaughtIntent();
                }
                break;
            case R.id.activity_data_input_catch_order_question_pic_yes:
                if (checked) {
                    mCatchOrderPicAns = "true";
                    picValid=true;
                    if(nValid && typeValid && priceValid && whereValid && picValid)
                        mButton.setEnabled(true);
                    else
                        mButton.setEnabled(false);
                    if(mCatchSalePicAns.equals("true")) LaunchFishCaughtIntent();
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;

        if(spin.getId() == R.id.activity_data_input_catch_order_input_price)
        {
            mCatchOrderPrice = prices[position];
            mCatchOrderPriceInt=position;
            if(mCatchOrderPrice.equals("Prix pour un"))
                priceValid = false;
            else
                priceValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }else{
            mCatchOrderWhere = places[position];
            mCatchOrderWhereInt=position;
            if(mCatchOrderWhere.equals("Choisi le lieu"))
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
            mCatchOrderN = newVal;
            if(mCatchOrderN==0)
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
            mCatchOrderType = type[newVal];
            mCatchOrderTypeInt = newVal;
            if(mCatchOrderType.equals("Choisi"))
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

        outState.putString(recopemValues.BUNDLE_STATE_ANS,mCatchOrderAns);
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchOrderN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchOrderTypeInt);
        outState.putInt(recopemValues.BUNDLE_STATE_PRICE_INT,mCatchOrderPriceInt);
        outState.putInt(recopemValues.BUNDLE_STATE_WHERE_INT,mCatchOrderWhereInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchOrderInputDetails.getText().toString());
        outState.putString(recopemValues.BUNDLE_STATE_PIC_ANS,mCatchOrderPicAns);
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS,mCatchSalePicAns);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchOrder.this, dataInputFishCaught.class);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
        fishCaughtIntent.putExtra(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        startActivity(fishCaughtIntent);
    }
}