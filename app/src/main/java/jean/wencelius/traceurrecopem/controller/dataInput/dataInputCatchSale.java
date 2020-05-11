package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackListActivity;

public class dataInputCatchSale extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private String mCatchSaleAns;
    private int mCatchSaleN;
    private String mCatchSaleType;
    private String mCatchSalePrice;
    private String mCatchSaleWhere;
    private String mCatchSaleDetails;
    private String mCatchSalePicAns;

    //Views
    private Button mButton;
    private RelativeLayout mCatchSaleQuantityFrame;
    private LinearLayout mCatchSalePicFrame;

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
        mButton.setEnabled(false);

        mCatchSaleQuantityFrame = (RelativeLayout) findViewById(R.id.activity_catch_sale_quantity_frame);
        mCatchSalePicFrame = (LinearLayout) findViewById(R.id.activity_catch_sale_pic_frame);

        mCatchSaleInputN = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_N);
        mCatchSaleInputType = (NumberPicker) findViewById(R.id.activity_data_input_catch_sale_input_type);
        mCatchSaleInputPrice = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_price);
        mCatchSaleInputWhere = (Spinner) findViewById(R.id.activity_data_input_catch_sale_input_where);
        mCatchSaleInputDetails = (EditText) findViewById(R.id.activity_data_input_catch_sale_input_details);

        mCatchSaleInputN.setMinValue(0);
        mCatchSaleInputN.setMaxValue(100);
        mCatchSaleInputN.setOnValueChangedListener(new nPicker());

        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mCatchSaleInputType.setMinValue(0);
        mCatchSaleInputType.setMaxValue(type.length-1);
        mCatchSaleInputType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
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

        mCatchSaleQuantityFrame.setVisibility(View.INVISIBLE);
        mCatchSalePicFrame.setVisibility(View.INVISIBLE);

        mCatchSaleAns = "empty";
        mCatchSaleN = 0;
        mCatchSaleType = "NA";
        mCatchSalePrice = "NA";
        mCatchSaleWhere = "NA";
        mCatchSalePicAns = "NA";
        mCatchSaleDetails = "NA";

        nValid = false;
        typeValid = false;
        priceValid = false;
        whereValid = false;
        picValid = false;


        //TODO:
        setTitle("Question 5/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCatchSaleAns.equals("false")){
                    //don't fill in mCrewN and mCrewWho
                }else{
                    //fill in mCrewN
                    mCatchSaleDetails = mCatchSaleInputDetails.getText().toString();
                }

                Toast.makeText(dataInputCatchSale.this, "N fish = " +
                        Integer.toString(mCatchSaleN) + " "+
                        mCatchSaleType +
                        "Price = "+mCatchSalePrice+" Where = "+mCatchSaleWhere + "Details = "+mCatchSaleDetails, Toast.LENGTH_LONG).show();

                //Intent NextIntent = new Intent(dataInputCatchSale.this, TrackListActivity.class);
                //startActivity(NextIntent);
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
                    mCatchSalePrice = "NA";
                    mCatchSaleWhere = "NA";
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
        Toast.makeText(this, mCatchSaleAns, Toast.LENGTH_SHORT).show();
    }

   @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;

        if(spin.getId() == R.id.activity_data_input_catch_sale_input_price)
        {
            Toast.makeText(this, "Your choice :" + prices[position],Toast.LENGTH_SHORT).show();
            mCatchSalePrice = prices[position];
            if(mCatchSalePrice.equals("Prix pour un"))
                priceValid = false;
            else
                priceValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
        }else{
            Toast.makeText(this, "Your choice :" + places[position],Toast.LENGTH_SHORT).show();
            mCatchSaleWhere = places[position];
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
            if(mCatchSaleType.equals("Choisi"))
                typeValid = false;
            else
                typeValid = true;

            if(nValid && typeValid && priceValid && whereValid && picValid)
                mButton.setEnabled(true);
            else
                mButton.setEnabled(false);
            Toast.makeText(dataInputCatchSale.this, mCatchSaleType, Toast.LENGTH_SHORT).show();
        }
    }
}
