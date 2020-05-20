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

    //TODO: Implement Add Manual Track Activity
    //TODO: Enable waypoint creation on MapTrack (Dialog to input name). Means to think of a way to: create a layout with all waypoints in DB, clickable, evenutally possibility to delete them
    //TODO: Status of emailSent in TrackDetailActivity not satisfying because appears as true even if action cancelled by user.

    //TODO: once all dataInput activities created, delete this one.

/*
Map And Track Activity
  => Find a way to create overlay from waypoints and refresh it when new waypoint created => HAve to make query on whole WAYPOINTS TBL and not only trackId's waypoint table
            => Create a button to display waypoints => waypoint markers should be clickable and on click link to delete waypoint.

*/

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
