package jean.wencelius.traceurrecopem.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputCatchOrder extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static dataInputCatchOrder orderAct;

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
    private String mSaveDir;

    //Views
    private RelativeLayout mCatchOrderQuantityFrame;
    private LinearLayout mCatchOrderPicFrame;

    private EditText mCatchOrderInputDetails;

    private String [] places;
    private String[] prices;
    private String [] type;

    private boolean nValid;
    private boolean typeValid;
    private boolean priceValid;
    private boolean whereValid;
    private boolean picValid;
    private boolean showNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_catch_order);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        orderAct = this;

        mCatchOrderQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_order_quantity_frame);
        mCatchOrderPicFrame = (LinearLayout) findViewById(R.id.activity_catch_order_pic_frame);

        mCatchOrderInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_order_input_details);

        mCatchDestination = "order";

        TextView mPicOrderQuestion = (TextView) findViewById(R.id.activity_data_input_catch_order_question_pic);

        RadioButton mCatchOrderInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_yes);
        RadioButton mCatchOrderInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_no);
        RadioButton mCatchOrderInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_pic_yes);
        RadioButton mCatchOrderInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_order_question_pic_no);

        NumberPicker mCatchOrderInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_order_input_N);
        NumberPicker mCatchOrderInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_order_input_type);
        Spinner mCatchOrderInputPrice = (Spinner) findViewById(R.id.activity_data_input_catch_order_input_price);
        Spinner mCatchOrderInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_order_input_where);

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

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
            mSaveDir = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SAVE_DIR);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);
            mCatchSalePicAns = getIntent().getExtras().getString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS);


            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String catchOrderAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER));
            int catchOrderN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_N));
            String catchOrderType = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_TYPE));
            String catchOrderPrice = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_PRICE));
            String catchOrderWhere = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_WHERE));
            String catchOrderDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_DETAILS));
            String catchOrderPicAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_PIC));

            mTrackCursor.close();
            if(catchOrderAns!=null){
                mCatchOrderAns = catchOrderAns;
                mCatchOrderN = catchOrderN;
                mCatchOrderType = catchOrderType;
                if (catchOrderType!=null){
                    mCatchOrderTypeInt = Arrays.asList(type).indexOf(catchOrderType);
                }else{
                    mCatchOrderTypeInt = 0;
                }
                mCatchOrderPrice = catchOrderPrice;
                if (catchOrderPrice!=null){
                    mCatchOrderPriceInt = Arrays.asList(prices).indexOf(catchOrderPrice);
                }else{
                    mCatchOrderPriceInt = 0;
                }
                mCatchOrderWhere = catchOrderWhere;
                if (catchOrderWhere!=null){
                    mCatchOrderWhereInt = Arrays.asList(places).indexOf(catchOrderWhere);
                }else{
                    mCatchOrderWhereInt = 0;
                }
                mCatchOrderPicAns = catchOrderPicAns;
                mCatchOrderDetails = catchOrderDetails;

            }else {
                mCatchOrderAns = "empty";
                mCatchOrderN = 0;
                mCatchOrderType = type[0];
                mCatchOrderTypeInt = 0;
                mCatchOrderPrice = prices[0];
                mCatchOrderPriceInt = 0;
                mCatchOrderPicAns = "NA";
                mCatchOrderDetails = "NA";

                catchOrderWhere = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF, getApplicationContext());
                if (null != catchOrderWhere) {
                    mCatchOrderWhere = catchOrderWhere;
                    mCatchOrderWhereInt = Arrays.asList(places).indexOf(catchOrderWhere);
                    mCatchOrderInputWhere.setSelection(mCatchOrderWhereInt);
                } else {
                    mCatchOrderWhere = places[0];
                    mCatchOrderWhereInt = 0;
                }
            }
        }

        mCatchOrderInputAnsY.setChecked(mCatchOrderAns.equals("true"));
        mCatchOrderInputAnsN.setChecked(mCatchOrderAns.equals("false"));
        mCatchOrderInputPicAnsY.setChecked(mCatchOrderPicAns.equals("true"));
        mCatchOrderInputPicAnsN.setChecked(mCatchOrderPicAns.equals("false"));

        mCatchOrderInputN.setValue(mCatchOrderN);
        mCatchOrderInputType.setValue(mCatchOrderTypeInt);
        mCatchOrderInputPrice.setSelection(mCatchOrderPriceInt);
        mCatchOrderInputWhere.setSelection(mCatchOrderWhereInt);

        if(!mCatchOrderDetails.equals("NA")){
            mCatchOrderInputDetails.setText(mCatchOrderDetails);
            mCatchOrderInputDetails.setSelection(mCatchOrderDetails.length());
        }

        nValid = mCatchOrderN!=0;
        typeValid = mCatchOrderTypeInt!=0;
        priceValid = mCatchOrderPriceInt!=0;
        whereValid = mCatchOrderWhereInt!=0;
        picValid = mCatchOrderPicAns.equals("true") || mCatchOrderPicAns.equals("false");

        showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
        invalidateOptionsMenu();

        if(mCatchOrderAns.equals("true")){
            mCatchOrderQuantityFrame.setVisibility(View.VISIBLE);
            mCatchOrderPicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchOrderQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchOrderPicFrame.setVisibility(View.INVISIBLE);
        }

        if(mCatchSalePicAns.equals("true")) mPicOrderQuestion.setText(R.string.data_input_catch_order_question_pic_if_sale_pic);

        setTitle("Question 6/8");
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_order_question_no:
                if (checked) {
                    mCatchOrderAns="false";
                    showNext = true;
                    mCatchOrderQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchOrderPicFrame.setVisibility(View.INVISIBLE);
                    mCatchOrderN = 0;
                    mCatchOrderType = type[0];
                    mCatchOrderTypeInt = 0;
                    mCatchOrderPrice = prices[0];
                    mCatchOrderPriceInt = 0;
                    mCatchOrderWhere = places[0];
                    mCatchOrderWhereInt = 0;
                    mCatchOrderPicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_order_question_yes:
                if (checked) {
                    mCatchOrderAns="true";
                    showNext = false;
                    mCatchOrderQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchOrderPicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_order_question_pic_no:
                if (checked) {
                    mCatchOrderPicAns = "false";
                    picValid=true;
                    showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
                    LaunchFishCaughtIntent();
                }
                break;
            case R.id.activity_data_input_catch_order_question_pic_yes:
                if (checked) {
                    mCatchOrderPicAns = "true";
                    picValid=true;
                   showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
                    if(mCatchSalePicAns.equals("true")) LaunchFishCaughtIntent();
                }
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;

        if(spin.getId() == R.id.activity_data_input_catch_order_input_price)
        {
            mCatchOrderPrice = prices[position];
            mCatchOrderPriceInt=position;
            priceValid = !mCatchOrderPrice.equals(prices[0]);
            showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
        }else{
            mCatchOrderWhere = places[position];
            mCatchOrderWhereInt=position;
            whereValid = !mCatchOrderWhere.equals(places[0]);
            showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        showNext = false;
        invalidateOptionsMenu();
    }

    class nPicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchOrderN = newVal;
            nValid = mCatchOrderN!=0;
           showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
           invalidateOptionsMenu();
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchOrderType = type[newVal];
            mCatchOrderTypeInt = newVal;
            typeValid = !mCatchOrderType.equals(type[0]);
            showNext = mCatchOrderAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
            invalidateOptionsMenu();
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

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(recopemValues.BUNDLE_STATE_SALE_PIC_ANS,mCatchSalePicAns);
        outState.putString(recopemValues.BUNDLE_STATE_SAVE_DIR,mSaveDir);
        super.onSaveInstanceState(outState);
    }

    private void LaunchFishCaughtIntent() {
        Intent fishCaughtIntent = new Intent(dataInputCatchOrder.this, dataInputFishCaught.class);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
        fishCaughtIntent.putExtra(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
        fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_DIR,mSaveDir);
        startActivity(fishCaughtIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.datainput_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.activity_data_input_menu_next).setVisible(showNext);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.activity_data_input_menu_next:
                mCatchOrderDetails = mCatchOrderInputDetails.getText().toString();

                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues catchOrderValues = new ContentValues();
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER,mCatchOrderAns);
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_N,mCatchOrderN);
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_TYPE,mCatchOrderType);
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_PRICE,mCatchOrderPrice);
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_WHERE,mCatchOrderWhere);
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_DETAILS,mCatchOrderDetails);
                catchOrderValues.put(TrackContentProvider.Schema.COL_CATCH_ORDER_PIC,mCatchOrderPicAns);

                getContentResolver().update(trackUri, catchOrderValues, null, null);

                Intent NextIntent = new Intent(dataInputCatchOrder.this, dataInputCatchGive.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_DIR,mSaveDir);

                NextIntent.putExtra(recopemValues.BUNDLE_STATE_SALE_PIC_ANS, mCatchSalePicAns);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_ORDER_PIC_ANS,mCatchOrderPicAns);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputCatchOrder getInstance(){
        return   orderAct;
    }
}