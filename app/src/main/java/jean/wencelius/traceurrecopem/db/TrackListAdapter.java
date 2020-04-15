package jean.wencelius.traceurrecopem.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.Track;

/**
 * Created by Jean Wencélius on 10/04/2020.
 */
public class TrackListAdapter extends CursorAdapter {

    public TrackListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bind(cursor, view, context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup vg) {
        View view = LayoutInflater.from(vg.getContext()).inflate(R.layout.tracklist_item,
                vg, false);
        return view;
    }

    /**
     * Do the binding between data and item view.
     *
     * @param cursor
     *				Cursor to pull data
     * @param v
     *				RelativeView representing one item
     * @param context
     *				Context, to get resources
     * @return The relative view with data bound.
     */
    private View bind(Cursor cursor, View v, Context context) {
        TextView vId = (TextView) v.findViewById(R.id.trackmgr_item_id);
        TextView vNameOrStartDate = (TextView) v.findViewById(R.id.trackmgr_item_nameordate);
        TextView vTps = (TextView) v.findViewById(R.id.trackmgr_item_tps);
        //ImageView vStatus = (ImageView) v.findViewById(R.id.trackmgr_item_statusicon);

        // Is track active ?
        /**int active = cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ACTIVE));
        if (TrackContentProvider.Schema.VAL_TRACK_ACTIVE == active) {
            // Yellow clock icon for Active
            vStatus.setImageResource(android.R.drawable.presence_away);
            vStatus.setVisibility(View.VISIBLE);
        } else if (cursor.isNull(cursor.getColumnIndex(TrackContentProvider.Schema.COL_EXPORT_DATE))) {
            // Hide green circle icon: Track not yet exported
            vStatus.setVisibility(View.GONE);
        } else {
            // Show green circle icon (don't assume already visible with this drawable; may be a re-query)
            vStatus.setImageResource(android.R.drawable.presence_online);
            vStatus.setVisibility(View.VISIBLE);
        }*/

        // Bind id
        long trackId = cursor.getLong(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID));
        String strTrackId = Long.toString(trackId);
        vId.setText("#" + strTrackId);

        // Bind WP count, TP count, name
        Track t = Track.build(trackId, cursor, context.getContentResolver(), false);
        vTps.setText(Integer.toString(t.getTpCount()));
        vNameOrStartDate.setText(t.getName());

        return v;
    }
}
