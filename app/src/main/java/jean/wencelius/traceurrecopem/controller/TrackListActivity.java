package jean.wencelius.traceurrecopem.controller;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.db.TrackListAdapter;
import jean.wencelius.traceurrecopem.recopemValues;

public class TrackListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));

        ImageButton mBtnBack = (ImageButton)  findViewById(R.id.activity_tracklist_home);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MenuActivityIntent = new Intent(TrackListActivity.this, MenuActivity.class);
                startActivity(MenuActivityIntent);
            }
        });
    }


    @Override
    protected void onResume() {
        Cursor cursor = getContentResolver().query(
                TrackContentProvider.CONTENT_URI_TRACK, null, null, null,
                TrackContentProvider.Schema.COL_START_DATE + " desc");

        startManagingCursor(cursor);
        setListAdapter(new TrackListAdapter(TrackListActivity.this, cursor));
        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));  // undo change from onPause
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long thisTrackId = id;
                new AlertDialog.Builder(TrackListActivity.this)
                        .setTitle(R.string.activity_track_detail_delete_dialog_title)
                        .setMessage(getResources().getString(R.string.activity_track_detail_delete_dialog_message))
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteTrack(thisTrackId);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
                return true;
            }
        });

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

    private void deleteTrack(long trackId){
        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId),
                null, null, null, null);
        cursor.moveToPosition(0);
        String saveDir = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DIR));
        cursor.close();

        getContentResolver().delete(
                ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId),
                null, null);

        // Delete any data stored for the track we're deleting
        if(saveDir!=null){
            File trackStorageDirectory = new File(saveDir);
            if (trackStorageDirectory.exists()) {
                boolean deleted = false;

                //If it's a directory and we should delete it recursively, try to delete all childs
                if(trackStorageDirectory.isDirectory()){
                    for(File child:trackStorageDirectory.listFiles()){
                        //deleted = child.delete();
                        child.delete();
                    }
                }
                //deleted = trackStorageDirectory.delete();
                trackStorageDirectory.delete();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

       ContentResolver cr = getContentResolver();
       Cursor cursor = cr.query(
                ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, id),
                null, null, null, null);

       cursor.moveToPosition(0);

       String picAdded = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_ADDED));
       String caughtFishDetails = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS));
       String dataAdded = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED));
       String exported = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_EXPORTED));
       String sentEmail = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_SENT_EMAIL));
       String saveDir = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DIR));

       cursor.close();

       Intent TrackDetailIntent = new Intent(TrackListActivity.this,TrackDetailActivity.class);

       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, id);
       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_PIC_ADDED,picAdded);
       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS,caughtFishDetails);
       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED, dataAdded);
       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_EXPORTED, exported);
       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_DIR,saveDir);
       TrackDetailIntent.putExtra(TrackContentProvider.Schema.COL_SENT_EMAIL,sentEmail);

       startActivity(TrackDetailIntent);

        Toast.makeText(this, "Track # "+Long.toString(id), Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onBackPressed() {
        Intent MenuActivityIntent = new Intent(TrackListActivity.this,MenuActivity.class);
        startActivity(MenuActivityIntent);
        super.onBackPressed();
    }
}
