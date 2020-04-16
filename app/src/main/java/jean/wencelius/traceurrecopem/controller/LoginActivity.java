package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.model.User;

public class LoginActivity extends AppCompatActivity {
    private EditText mFisherNameInput;
    private EditText mFisherIdInput;
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
        mSubmitButton = (Button) findViewById(R.id.activity_login_submit_btn);

        mSubmitButton.setEnabled(false);

        mUser = new User();

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

                mUser.setFisherName(fishername);
                mUser.setFisherId(fisherid);

                AppPreferences.setDefaultsString("PREF_KEY_FISHER_NAME",fishername,getApplicationContext());
                AppPreferences.setDefaultsString("PREF_KEY_FISHER_ID",fisherid,getApplicationContext());

                //User clicked button
                Intent menuActivityIntent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(menuActivityIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
