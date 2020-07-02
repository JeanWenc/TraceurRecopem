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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputCatchSale extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static dataInputCatchSale saleAct;

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
    private String mSaveDir;

    //Views
    private RelativeLayout mCatchSaleQuantityFrame;
    private LinearLayout mCatchSalePicFrame;

    private EditText mCatchSaleInputDetails;

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
        setContentView(R.layout.activity_data_input_catch_sale);

        //Prevent keyboard from showing up on activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        saleAct = this;

        mCatchSaleQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_sale_quantity_frame);
        mCatchSalePicFrame = (LinearLayout) findViewById(R.id.activity_catch_sale_pic_frame);

        mCatchSaleInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_sale_input_details);

        mCatchDestination = "sale";

        RadioButton mCatchSaleInputAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_yes);
        RadioButton mCatchSaleInputAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_no);
        RadioButton mCatchSaleInputPicAnsY = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_pic_yes);
        RadioButton mCatchSaleInputPicAnsN = (RadioButton) findViewById(R.id.activity_data_input_catch_sale_question_pic_no);

        NumberPicker mCatchSaleInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_N);
        NumberPicker mCatchSaleInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_type);
        Spinner mCatchSaleInputPrice = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_price);
        Spinner mCatchSaleInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_where);

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
            mCatchSaleAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_ANS);
            mCatchSaleN = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_CATCH_N);
            mCatchSaleTypeInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_TYPE_INT);
            mCatchSaleType = type[mCatchSaleTypeInt];
            mCatchSalePriceInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_PRICE_INT);
            mCatchSalePrice = prices[mCatchSalePriceInt];
            mCatchSaleWhereInt = savedInstanceState.getInt(recopemValues.BUNDLE_STATE_WHERE_INT);
            mCatchSaleWhere = places[mCatchSaleWhereInt];
            mCatchSalePicAns = savedInstanceState.getString(recopemValues.BUNDLE_STATE_PIC_ANS);
            mCatchSaleDetails = savedInstanceState.getString(recopemValues.BUNDLE_STATE_DETAILS);

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);
            mSaveDir = savedInstanceState.getString(recopemValues.BUNDLE_STATE_SAVE_DIR);

        }else{
            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
            mSaveDir = getIntent().getExtras().getString(TrackContentProvider.Schema.COL_DIR);

            Cursor mTrackCursor = getContentResolver().query(ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId), null, null, null, null);
            mTrackCursor.moveToPosition(0);

            String catchSaleAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE));
            int catchSaleN = mTrackCursor.getInt(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_N));
            String catchSaleType = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_TYPE));
            String catchSalePrice = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_PRICE));
            String catchSaleWhere = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_WHERE));
            String catchSaleDetails = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS));
            String catchSalePicAns = mTrackCursor.getString(mTrackCursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_PIC));

            mTrackCursor.close();

            if(catchSaleAns!=null){
                mCatchSaleAns = catchSaleAns;
                mCatchSaleN = catchSaleN;
                mCatchSaleType = catchSaleType;
                if (catchSaleType!=null){
                    mCatchSaleTypeInt = Arrays.asList(type).indexOf(catchSaleType);
                }else{
                    mCatchSaleTypeInt = 0;
                }
                mCatchSalePrice = catchSalePrice;
                if (catchSalePrice!=null){
                    mCatchSalePriceInt = Arrays.asList(prices).indexOf(catchSalePrice);
                }else{
                    mCatchSalePriceInt = 0;
                }
                mCatchSaleWhere = catchSaleWhere;
                if (catchSaleWhere!=null){
                    mCatchSaleWhereInt = Arrays.asList(places).indexOf(catchSaleWhere);
                }else{
                    mCatchSaleWhereInt = 0;
                }
                mCatchSalePicAns = catchSalePicAns;
                mCatchSaleDetails = catchSaleDetails;
            }else{
                mCatchSaleAns = "empty";
                mCatchSaleN = 0;
                mCatchSaleType = type[0];
                mCatchSaleTypeInt = 0;
                mCatchSalePrice = prices[0];
                mCatchSalePriceInt = 0;
                mCatchSalePicAns = "NA";
                mCatchSaleDetails = "NA";

                catchSaleWhere = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF,getApplicationContext());
                if(null!=catchSaleWhere){
                    mCatchSaleWhere = catchSaleWhere;
                    mCatchSaleWhereInt = Arrays.asList(places).indexOf(catchSaleWhere);
                    mCatchSaleInputWhere.setSelection(mCatchSaleWhereInt);
                }else{
                    mCatchSaleWhere = places[0];
                    mCatchSaleWhereInt = 0;
                }
            }
        }

        mCatchSaleInputAnsY.setChecked(mCatchSaleAns.equals("true"));
        mCatchSaleInputAnsN.setChecked(mCatchSaleAns.equals("false"));
        mCatchSaleInputPicAnsY.setChecked(mCatchSalePicAns.equals("true"));
        mCatchSaleInputPicAnsN.setChecked(mCatchSalePicAns.equals("false"));

        mCatchSaleInputN.setValue(mCatchSaleN);
        mCatchSaleInputType.setValue(mCatchSaleTypeInt);
        mCatchSaleInputPrice.setSelection(mCatchSalePriceInt);
        mCatchSaleInputWhere.setSelection(mCatchSaleWhereInt);

        if(!mCatchSaleDetails.equals("NA")){
            mCatchSaleInputDetails.setText(mCatchSaleDetails);
            mCatchSaleInputDetails.setSelection(mCatchSaleDetails.length());
        }

        nValid = mCatchSaleN!=0;
        typeValid = mCatchSaleTypeInt!=0;
        priceValid = mCatchSalePriceInt!=0;
        whereValid = mCatchSaleWhereInt!=0;
        picValid = mCatchSalePicAns.equals("true") || mCatchSalePicAns.equals("false");

        showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
        invalidateOptionsMenu();

        if(mCatchSaleAns.equals("true")){
            mCatchSaleQuantityFrame.setVisibility(View.VISIBLE);
            mCatchSalePicFrame.setVisibility(View.VISIBLE);
        }else{
            mCatchSaleQuantityFrame.setVisibility(View.INVISIBLE);
            mCatchSalePicFrame.setVisibility(View.INVISIBLE);
        }

        setTitle("Question 5/8");
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.activity_data_input_catch_sale_question_no:
                if (checked) {
                    mCatchSaleAns="false";
                    showNext = true;
                    mCatchSaleQuantityFrame.setVisibility(View.INVISIBLE);
                    mCatchSalePicFrame.setVisibility(View.INVISIBLE);
                    mCatchSaleN = 0;
                    mCatchSaleType = type[0];
                    mCatchSaleTypeInt = 0;
                    mCatchSalePrice = prices[0];
                    mCatchSalePriceInt = 0;
                    mCatchSaleWhere = places[0];
                    mCatchSaleWhereInt = 0;
                    mCatchSalePicAns = "false";
                }
                break;
            case R.id.activity_data_input_catch_sale_question_yes:
                if (checked) {
                    mCatchSaleAns="true";
                    showNext = false;
                    mCatchSaleQuantityFrame.setVisibility(View.VISIBLE);
                    mCatchSalePicFrame.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.activity_data_input_catch_sale_question_pic_no:
                if (checked) {
                    mCatchSalePicAns = "false";
                    picValid=true;

                    showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);

                    Intent fishCaughtIntent = new Intent(dataInputCatchSale.this, dataInputFishCaught.class);
                    fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                    fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                    fishCaughtIntent.putExtra(recopemValues.BUNDLE_EXTRA_CATCH_DESTINATION,mCatchDestination);
                    fishCaughtIntent.putExtra(TrackContentProvider.Schema.COL_DIR,mSaveDir);
                    startActivity(fishCaughtIntent);
                }
                break;
            case R.id.activity_data_input_catch_sale_question_pic_yes:
                if (checked) {
                    mCatchSalePicAns = "true";
                    picValid=true;
                   showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
                }
                break;
        }
        invalidateOptionsMenu();
    }

   @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;

        if(spin.getId() == R.id.activity_data_input_catch_sale_input_price)
        {
            mCatchSalePrice = prices[position];
            mCatchSalePriceInt=position;

            priceValid = !mCatchSalePrice.equals(prices[0]);

            showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
        }else{
            mCatchSaleWhere = places[position];
            mCatchSaleWhereInt=position;

            whereValid = !mCatchSaleWhere.equals(places[0]);

            showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
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
            mCatchSaleN = newVal;
            nValid = mCatchSaleN!=0;
            showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
            invalidateOptionsMenu();
        }
    }

    class typePicker implements NumberPicker.OnValueChangeListener{
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mCatchSaleType = type[newVal];
            mCatchSaleTypeInt = newVal;
            typeValid = !mCatchSaleType.equals(type[0]);
           showNext = mCatchSaleAns.equals("false") || (nValid && typeValid && priceValid && whereValid && picValid);
           invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString(recopemValues.BUNDLE_STATE_ANS,mCatchSaleAns);
        outState.putInt(recopemValues.BUNDLE_STATE_CATCH_N,mCatchSaleN);
        outState.putInt(recopemValues.BUNDLE_STATE_TYPE_INT,mCatchSaleTypeInt);
        outState.putInt(recopemValues.BUNDLE_STATE_PRICE_INT,mCatchSalePriceInt);
        outState.putInt(recopemValues.BUNDLE_STATE_WHERE_INT,mCatchSaleWhereInt);
        outState.putString(recopemValues.BUNDLE_STATE_DETAILS,mCatchSaleInputDetails.getText().toString());
        outState.putString(recopemValues.BUNDLE_STATE_PIC_ANS,mCatchSalePicAns);

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);
        outState.putString(recopemValues.BUNDLE_STATE_SAVE_DIR,mSaveDir);
        super.onSaveInstanceState(outState);
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

                Intent NextIntent = new Intent(dataInputCatchSale.this, dataInputCatchOrder.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_DIR,mSaveDir);
                NextIntent.putExtra(recopemValues.BUNDLE_STATE_SALE_PIC_ANS, mCatchSalePicAns);
                startActivity(NextIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static dataInputCatchSale getInstance(){
        return   saleAct;
    }
}
