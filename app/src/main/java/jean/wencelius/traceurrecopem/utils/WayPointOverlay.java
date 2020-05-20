package jean.wencelius.traceurrecopem.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;

/**
 * Created by Jean Wencélius on 20/05/2020.
 */
public class WayPointOverlay extends ItemizedOverlay<OverlayItem> {

    private List<OverlayItem> wayPointItems = new ArrayList<OverlayItem>();

    private long maxTrackId;

    private ContentResolver mCr;

    public WayPointOverlay(final Drawable pDefaultMarker, final Context ctx,  final long maxTrackId) {
        super(pDefaultMarker);
        this.maxTrackId = maxTrackId;
        this.mCr=ctx.getContentResolver();
        refresh();
    }
    public WayPointOverlay(final Context ctx, final long maxTrackId){
        this(ctx.getResources().getDrawable(R.drawable.star), ctx, maxTrackId);
    }


    @Override
    protected OverlayItem createItem(int i) {
        return wayPointItems.get(i);
    }

    @Override
    public int size() {
        return wayPointItems.size();
    }

    @Override
    public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
        return false;
    }

    public void refresh() {
        wayPointItems.clear();

        Cursor c = this.mCr.query(TrackContentProvider.waypointsUri(recopemValues.MAX_TRACK_ID),null,null,null,null);

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            OverlayItem i = new OverlayItem(
                    c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_NAME)),
                    c.getString(c.getColumnIndex(TrackContentProvider.Schema.COL_NAME)),
                    new GeoPoint(
                            c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LATITUDE)),
                            c.getDouble(c.getColumnIndex(TrackContentProvider.Schema.COL_LONGITUDE)))
            );
            wayPointItems.add(i);
        }
        c.close();
        populate();
    }
}
