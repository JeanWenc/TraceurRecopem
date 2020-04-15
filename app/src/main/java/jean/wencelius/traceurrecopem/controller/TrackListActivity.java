package jean.wencelius.traceurrecopem.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ListView;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.db.TrackListAdapter;

public class TrackListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        getListView().setEmptyView(findViewById(R.id.trackmgr_empty));
    }

    @Override
    protected void onResume() {
        Cursor cursor = getContentResolver().query(
                TrackContentProvider.CONTENT_URI_TRACK, null, null, null,
                TrackContentProvider.Schema.COL_START_DATE + " desc");
        startManagingCursor(cursor);
        setListAdapter(new TrackListAdapter(TrackListActivity.this, cursor));
        getListView().setEmptyView(findViewById(R.id.trackmgr_empty));  // undo change from onPause

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
}
