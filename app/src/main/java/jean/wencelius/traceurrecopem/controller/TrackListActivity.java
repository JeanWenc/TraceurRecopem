package jean.wencelius.traceurrecopem.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.db.TrackListAdapter;
import jean.wencelius.traceurrecopem.gpx.ExportToStorageTask;

public class TrackListActivity extends ListActivity {

    private ImageButton mBtnAddManualTrack;
    private ImageButton mBtnExportAllTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));

        mBtnAddManualTrack = (ImageButton) findViewById(R.id.activity_tracklist_add_track_manual);
        mBtnExportAllTracks = (ImageButton) findViewById(R.id.activity_tracklist_export_all_tracks);

        mBtnAddManualTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createManualTrack();
            }
        });

        mBtnExportAllTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportAll();
            }
        });

        //registerForContextMenu(getListView());
    }


    @Override
    protected void onResume() {
        Cursor cursor = getContentResolver().query(
                TrackContentProvider.CONTENT_URI_TRACK, null, null, null,
                TrackContentProvider.Schema.COL_START_DATE + " desc");
        startManagingCursor(cursor);
        setListAdapter(new TrackListAdapter(TrackListActivity.this, cursor));
        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));  // undo change from onPause

        super.onResume();
    }

    @Override
    protected void onPause() {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        if (adapter != null) {
            // Prevents on-screen 'no tracks' message
            getListView().setEmptyView(findViewById(android.R.id.empty));
            // Properly close the adapter cursor
            Cursor cursor = adapter.getCursor();
            stopManagingCursor(cursor);
            cursor.close();
            setListAdapter(null);
        }
        super.onPause();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Intent TrackListDetailIntent = new Intent(TrackListActivity.this,TrackDetailActivity.class);
        TrackListDetailIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, id);
        startActivity(TrackListDetailIntent);
    }

    private void createManualTrack() {
    }
    private void exportAll() {
    }
}
