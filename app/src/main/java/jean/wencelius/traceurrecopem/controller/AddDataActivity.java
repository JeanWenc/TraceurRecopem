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
    //TODO: Export to gpx on finish tracking just to ensure no data loss
    //TODO: If pictures true & AddData true => enable emailing (replace save by email, because normally track & csv generated at end of activities of dataInput activities and tracking activities)
    //TODO: For emailing implies zipping folder
    //TODO: Issue on StartDate (probably doesn't matter, also discard unncessary colums from DB (pic, scale, hour start, hour end)).
    //TODO: Implement Add Manual Track Activity
    //TODO: Enable waypoint creation on MapTrack (Dialog to input name). Means to think of a way to: create a layout with all waypoints in DB, clickable, evenutally possibility to delete them


    //TODO: once all dataInput activities created, delete this one.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);
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
