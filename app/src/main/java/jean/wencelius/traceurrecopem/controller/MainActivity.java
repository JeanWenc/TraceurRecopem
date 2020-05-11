package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;

public class MainActivity extends AppCompatActivity {

    private TextView mGreetingText;

    private int SPLASH_TIME = 3000;

    private String mFisherName;

    private String mPrefLocSale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mGreetingText = (TextView) findViewById(R.id.activity_main_greeting_text);

        mFisherName = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_NAME,getApplicationContext());
        mPrefLocSale = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_LOCATION_SALE_PREF, getApplicationContext());


        if(mFisherName!=null){
            String fulltext = "Ia Ora " + mFisherName + " !";
            mGreetingText.setText(fulltext);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mFisherName!=null && mPrefLocSale!=null){
                        Intent menuActivityIntent = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(menuActivityIntent);
                    }else{
                        Intent loginActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(loginActivityIntent);
                    }
                }// end tryCatch
            }//end run()
        };//end timerThread

        timer.start();
    }
}
