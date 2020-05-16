package jean.wencelius.traceurrecopem.controller.dataInput;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;

public class dataInputGear extends AppCompatActivity {

    private EditText mInputOtherDetail;
    private String mGear;
    private String mOtherDetail;
    private Button mButton;

    private long trackId;
    private boolean mNewPicAdded;

    private static final String BUNDLE_STATE_GEAR = "gear";
    private static final String BUNDLE_STATE_OTHER_DETAIL = "otherDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_gear);

        mButton = (Button) findViewById(R.id.activity_data_input_gear_next_btn);
        mInputOtherDetail = (EditText) findViewById(R.id.activity_data_input_gear_autre_detail);

        if(savedInstanceState != null){
            mGear = savedInstanceState.getString(BUNDLE_STATE_GEAR);
            mOtherDetail = savedInstanceState.getString(BUNDLE_STATE_OTHER_DETAIL);
            if(mGear.contains("other")){
                mInputOtherDetail.setVisibility(View.VISIBLE);
                mInputOtherDetail.setText(mOtherDetail);
                mInputOtherDetail.setSelection(mOtherDetail.length());
            }else{
                mInputOtherDetail.setVisibility(View.INVISIBLE);
            }
            mButton.setEnabled(savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON));

            trackId = savedInstanceState.getLong(recopemValues.BUNDLE_STATE_TRACK_ID);
            mNewPicAdded = savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED);

        }else{
            mGear="empty";
            mOtherDetail = "";
            mInputOtherDetail.setVisibility(View.INVISIBLE);
            mButton.setEnabled(false);

            trackId = getIntent().getExtras().getLong(TrackContentProvider.Schema.COL_TRACK_ID);
            mNewPicAdded = getIntent().getExtras().getBoolean(TrackContentProvider.Schema.COL_PIC_ADDED);
        }

        //TODO:
        setTitle("Question 1/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

                ContentValues gearValues = new ContentValues();
                gearValues.put(TrackContentProvider.Schema.COL_GEAR,mGear);

                if(mGear.contains("other")){
                    mOtherDetail = mInputOtherDetail.getText().toString();
                    gearValues.put(TrackContentProvider.Schema.COL_GEAR_OTHER_DETAILS,mOtherDetail);
                }

                getContentResolver().update(trackUri, gearValues, null, null);

                String textToDisplay ="Gear Type = " + mGear + "\n" +
                        "Other Details = " +  mOtherDetail;

                Toast.makeText(dataInputGear.this, textToDisplay, Toast.LENGTH_SHORT).show();

                Intent NextIntent = new Intent(dataInputGear.this, dataInputBoat.class);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                NextIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED, mNewPicAdded);
                startActivity(NextIntent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_STATE_GEAR,mGear);
        outState.putString(BUNDLE_STATE_OTHER_DETAIL,mInputOtherDetail.getText().toString());
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,mButton.isEnabled());

        outState.putLong(recopemValues.BUNDLE_STATE_TRACK_ID,trackId);
        outState.putBoolean(recopemValues.BUNDLE_STATE_NEW_PIC_ADDED,mNewPicAdded);

        super.onSaveInstanceState(outState);
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.activity_data_input_gear_fusil:
                if (checked){

                    if(!mGear.equals("empty"))
                       mGear+=" & spear";
                    else
                        mGear="spear";
                }else{
                    if(mGear.contains("spear")) removeString("spear");
                }
                break;
            case R.id.activity_data_input_gear_filet:
                if (checked){
                    if(!mGear.equals("empty"))
                        mGear+=" & net";
                    else
                        mGear="net";
                }else{
                    if(mGear.contains("net")) removeString("net");
                }

                break;
            case R.id.activity_data_input_gear_ligne:
                if (checked){
                    if(!mGear.equals("empty"))
                        mGear+=" & line";
                    else
                        mGear="line";
                }else{
                    if(mGear.contains("line")) removeString("line");
                }
                break;
            case R.id.activity_data_input_gear_rama:
                if (checked){
                    if(!mGear.equals("empty"))
                        mGear+=" & invertebrate";
                    else
                        mGear="invertebrate";
                }else{
                    if(mGear.contains("invertebrate")) removeString("invertebrate");
                }
                break;
            case R.id.activity_data_input_gear_cage:
                if (checked){
                    if(!mGear.equals("empty"))
                        mGear+=" & cage";
                    else
                        mGear="cage";
                }else{
                    if(mGear.contains("cage")) removeString("cage");
                }
                break;

            case R.id.activity_data_input_gear_harpon:
                if (checked){
                    if(!mGear.equals("empty"))
                        mGear+=" & harpoon";
                    else
                        mGear="harpoon";
                }else{
                    if(mGear.contains("harpoon")) removeString("harpoon");
                }
                break;

            case R.id.activity_data_input_gear_autre:
                if (checked){
                    mInputOtherDetail.setVisibility(View.VISIBLE);
                    if(!mGear.equals("empty"))
                        mGear+=" & other";
                    else
                        mGear="other";
                }else{
                    mInputOtherDetail.getText().clear();
                    mInputOtherDetail.setVisibility(View.INVISIBLE);
                    if(mGear.contains("other")) removeString("other");
                }
                break;
        }

        if(mGear.contains("other")){
            if (mInputOtherDetail.getText().toString().equals(""))
                mButton.setEnabled(false);
            else
                mButton.setEnabled(true);
            mInputOtherDetail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mButton.setEnabled(s.toString().length()!=0);
                }
                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }else{
            mButton.setEnabled(mGear!="empty");
        }
    }

    public void removeString(String string){
        if(mGear.contains(" & "+string)){
            mGear=mGear.replace(" & "+string,"");
        }else if(mGear.contains(string+" & ")){
            mGear=mGear.replace(string + " & ","");
        }else{
            mGear="empty";
        }
    }
}
