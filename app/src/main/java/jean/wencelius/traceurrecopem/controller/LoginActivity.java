package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.model.User;
import jean.wencelius.traceurrecopem.recopemValues;

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText mFisherNameInput;
    private EditText mFisherIdInput;
    private Spinner mBoatSpinner;
    private Spinner mSaleLocationSpinner;
    private CheckBox mBoatOwnerInput;

    private String mBoat;
    private String mBoatOwner;
    private String mLocation;


    private String [] boats;
    private String [] locations;

    private Button mSubmitButton;

    private User mUser;

    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFisherNameInput = (EditText) findViewById(R.id.activity_login_name_input);
        mFisherIdInput = (EditText) findViewById(R.id.activity_login_id_input);
        mBoatSpinner = (Spinner) findViewById(R.id.activity_login_boat);
        mBoatOwnerInput = (CheckBox) findViewById(R.id.activity_login_boat_owner);
        mSaleLocationSpinner = (Spinner) findViewById(R.id.activity_login_where_sale);
        mSubmitButton = (Button) findViewById(R.id.activity_login_submit_btn);

        mUser = new User();

        mBoat = "None";
        mBoatOwner = "NA";
        mLocation = "Choisi le lieu";

        mSubmitButton.setEnabled(false);

        boats = this.getResources().getStringArray(R.array.activity_login_boat_list);
        ArrayAdapter<CharSequence> boatAdapter = ArrayAdapter.createFromResource(this, R.array.activity_login_boat_list,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        boatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mBoatSpinner.setAdapter(boatAdapter);
        mBoatSpinner.setOnItemSelectedListener(this);

        locations = this.getResources().getStringArray(R.array.data_input_catch_sale_where);
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this, R.array.data_input_catch_sale_where,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSaleLocationSpinner.setAdapter(locationAdapter);
        mSaleLocationSpinner.setOnItemSelectedListener(this);




        mFisherIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSubmitButton.setEnabled(s.toString().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fishername = mFisherNameInput.getText().toString();
                String fisherid = mFisherIdInput.getText().toString();
                String boat = mBoat;
                String boatOwner = mBoatOwner;
                String location = mLocation;

                mUser.setFisherName(fishername);
                mUser.setFisherId(fisherid);

                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_NAME,fishername,getApplicationContext());
                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_ID,fisherid,getApplicationContext());
                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT,boat,getApplicationContext());
                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_BOAT_OWNER,boatOwner,getApplicationContext());
                AppPreferences.setDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF,location,getApplicationContext());

                //User clicked button
                Intent menuActivityIntent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(menuActivityIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner) parent;

        if (spin.getId() == R.id.activity_login_boat) {
            Toast.makeText(this, "Your choice :" + boats[position], Toast.LENGTH_SHORT).show();
            mBoat = boats[position];
        } else {
            Toast.makeText(this, "Your choice :" + locations[position], Toast.LENGTH_SHORT).show();
            mLocation = locations[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.activity_login_boat_owner:
                if (checked) {
                    mBoatOwner = "true";
                } else {
                    mBoatOwner = "false";
                }
                break;
        }
        Toast.makeText(this, "Boat Owner = " + mBoatOwner, Toast.LENGTH_SHORT).show();
    }
}
