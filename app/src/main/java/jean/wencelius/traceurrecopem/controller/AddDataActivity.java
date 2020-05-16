package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
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
import jean.wencelius.traceurrecopem.db.TrackContentProvider;

public class AddDataActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {
    //TODO: Add catchOrder, catchGive, catch Cons
    //TODO: On final activity get saveDir from cursor... export all answers to .csv (think about retrieving start and end time (make sure trackpoints > 1)
    //TODO: on final DataActivity => AddData = true
    //TODO: Export to gpx on finish tracking just to ensure no data loss
    //TODO: If pictures true & AddData true => enable emailing (replace save by email, because normally track & csv generated at end of activities of dataInput activities and tracking activities)
    //TODO: remove 'OFCFP from header of Track List Activity'. Remove Export Buttons
    //TODO: Implement Add Manual Track Activity
    //TODO: Enable waypoint creation on MapTrack (Dialog to input name). Means to think of a way to: create a layout with all waypoints in DB, clickable, evenutally possibility to delete them


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

    //TODO: Last Activity create textfile. Use the below as cursor
    /*ContentResolver cr = getContentResolver();
    Cursor cursor = cr.query(
            ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, id),
            null, null, null, null);

       cursor.moveToPosition(0);

    String picAdded = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_ADDED));
    String dataAdded = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED));

        cursor.close();
    */

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
