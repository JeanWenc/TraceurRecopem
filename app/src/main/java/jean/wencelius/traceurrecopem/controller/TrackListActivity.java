package jean.wencelius.traceurrecopem.controller;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.db.TrackListAdapter;
import jean.wencelius.traceurrecopem.gpx.ExportToStorageTask;
import jean.wencelius.traceurrecopem.recopemValues;

public class TrackListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        getListView().setEmptyView(findViewById(R.id.activity_tracklist_empty));

        ImageButton mBtnBack = (ImageButton)  findViewById(R.id.activity_tracklist_home);
        ImageButton mExportAll = (ImageButton) findViewById(R.id.activity_tracklist_export_all);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MenuActivityIntent = new Intent(TrackListActivity.this, MenuActivity.class);
                startActivity(MenuActivityIntent);
            }
        });

        mExportAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor c = getContentResolver().query(TrackContentProvider.CONTENT_URI_TRACKPOINT, null,
                        null, null, TrackContentProvider.Schema.COL_TIMESTAMP + " asc");
                c.moveToLast();
                String maxId = c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_ID));
                c.close();

                Toast.makeText(TrackListActivity.this, maxId, Toast.LENGTH_LONG).show();
                String mSaveDir = null;
                for(int i=1; i<= Integer.parseInt(maxId);i++){
                    mSaveDir = createDataTrackDirectory(i,TrackListActivity.this);
                    //Toast.makeText(TrackListActivity.this, mSaveDir, Toast.LENGTH_SHORT).show();
                    new ExportToStorageTask(TrackListActivity.this, mSaveDir, true,(long) i).execute();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        String [] selArgs = {"confirmed"};

        Cursor cursor = getContentResolver().query(
                TrackContentProvider.CONTENT_URI_TRACK, null, TrackContentProvider.Schema.COL_SENT_EMAIL+" != ?", selArgs,
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
        Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackContentProvider.Schema.COL_SENT_EMAIL,"confirmed");
        getContentResolver().update(trackUri, contentValues, null, null);

        Cursor cursor = getContentResolver().query(
                trackUri,
                null, null, null, null);
        cursor.moveToPosition(0);
        String saveDir = cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DIR));
        cursor.close();

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

    private static String createDataTrackDirectory(int trackId, Context ctx){

        File sdRoot = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            sdRoot = ctx.getExternalFilesDir(null);
            assert sdRoot!= null;
            if(!sdRoot.exists()){
                if(sdRoot.mkdirs()){
                }
            }
        }else{
            sdRoot = Environment.getExternalStorageDirectory();
        }

        int nRep =  5-String.valueOf(trackId).length();
        String repeated = new String(new char[nRep]).replace("\0","0");
        String perTrackDirectory = File.separator + "00_All" + File.separator + "Track"+ repeated +trackId;

        String trackGPXExportDirectory = new String();
        if (android.os.Build.MODEL.equals(recopemValues.Devices.NEXUS_S)) {
            // exportDirectoryPath always starts with "/"
            trackGPXExportDirectory = perTrackDirectory;
        }else{
            // Create a file based on the path we've generated above
            trackGPXExportDirectory = sdRoot + perTrackDirectory;
        }

        File storageDir = new File(trackGPXExportDirectory);

        if (! storageDir.exists()) {
            if (! storageDir.mkdirs()) {
                //Toast.makeText(this, "Directory [" + storageDir.getAbsolutePath() + "] does not exist and cannot be created", Toast.LENGTH_LONG).show();
            }else{
                File noMedia = new File(storageDir,".nomedia");
                try {
                    FileWriter writer = new FileWriter(noMedia,false);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                }
            }
        }

        return trackGPXExportDirectory;
    }

    @Override
    public void onBackPressed() {
        Intent MenuActivityIntent = new Intent(TrackListActivity.this,MenuActivity.class);
        startActivity(MenuActivityIntent);
        super.onBackPressed();
    }
}
