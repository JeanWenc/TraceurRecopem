package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;

public class AddDataActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {
//TODO: once all dataInput activities created, delete this one.


    private int mCatchOrder;
    private String mCatchOrderType;
    private Spinner mCatchOrderTypeSpinner;
    private String mCatchOrderWhere;
    private Spinner mCatchOrderWhereSpinner;
    //TODO:On Picture

    private int mCatchGive;
    private String mCatchGiveType;
    private Spinner mCatchGiveTypeSpinner;
    private String mCatchGiveWhere;
    private Spinner mCatchGiveWhereSpinner;

    private int mCatchCons;
    private String mCatchConsType;
    private Spinner mCatchConsTypeSpinner;

    //TODO: Question As-tu pris les poissons en photo? If no, question group of species per group of species

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        /*dayNight = (Spinner) findViewById(R.id.activity_add_data_day_night);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.day_night, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        dayNight.setAdapter(adapter);
        dayNight.setOnItemSelectedListener(this);*/
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
