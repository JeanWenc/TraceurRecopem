package jean.wencelius.traceurrecopem.controller.dataInput;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.TrackDetailActivity;

public class dataInputGear extends AppCompatActivity {

    private EditText mAutreDetail;

    public String mGear;

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input_gear);

        mButton = (Button) findViewById(R.id.activity_data_input_gear_next_btn);
        mButton.setEnabled(false);

        mAutreDetail = (EditText) findViewById(R.id.activity_data_input_gear_autre_detail);

        mGear="empty";
        mAutreDetail.setVisibility(View.INVISIBLE);

        //TODO:
        setTitle("Question 1/X");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent NextIntent = new Intent(dataInputGear.this, dataInputBoat.class);
                startActivity(NextIntent);
            }
        });

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
                    mAutreDetail.setVisibility(View.VISIBLE);
                    if(!mGear.equals("empty"))
                        mGear+=" & other";
                    else
                        mGear="other";
                }else{
                    mAutreDetail.getText().clear();
                    mAutreDetail.setVisibility(View.INVISIBLE);
                    if(mGear.contains("other")) removeString("other");
                }
                break;
        }

        if(mGear.contains("other")){
            mAutreDetail.addTextChangedListener(new TextWatcher() {
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

        //Toast.makeText(dataInputGear.this, mGear, Toast.LENGTH_LONG).show();
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
