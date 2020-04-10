package jean.wencelius.traceurrecopem.model;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import jean.wencelius.traceurrecopem.recopemValues;

public class TrackContentProvider extends ContentProvider {
    private static final String TAG = TrackContentProvider.class.getSimpleName();

    /*** Authority for Uris*/
    public static final String AUTHORITY = recopemValues.class.getPackage().getName() + ".provider";

    /**Uri for track*/
    public static final Uri CONTENT_URI_TRACK = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_TRACK);

    /**Uri for the active track*/
    public static final Uri CONTENT_URI_TRACK_ACTIVE = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_TRACK + "/active");

    /**Uri for a specific waypoint*/
    public static final Uri CONTENT_URI_WAYPOINT_UUID = Uri.parse("content://" + AUTHORITY + "/" + Schema.TBL_WAYPOINT + "/uuid");

    /**tables and joins to be used within a query to get the important informations of a track*/
    private static final String TRACK_TABLES = Schema.TBL_TRACK + " left join " + Schema.TBL_TRACKPOINT + " on " + Schema.TBL_TRACK + "." + Schema.COL_ID + " = " + Schema.TBL_TRACKPOINT + "." + Schema.COL_TRACK_ID;

    /*** the projection to be used to get the important informations of a track*/
    private static final String[] TRACK_TABLES_PROJECTION = {
            Schema.TBL_TRACK + "." + Schema.COL_ID + " as " + Schema.COL_ID,
            Schema.COL_ACTIVE,
            Schema.TBL_TRACK + "." + Schema.COL_NAME + " as "+ Schema.COL_NAME,
            Schema.COL_START_DATE,
            "count(" + Schema.TBL_TRACKPOINT + "." + Schema.COL_ID + ") as " + Schema.COL_TRACKPOINT_COUNT,
            "(SELECT count("+Schema.TBL_WAYPOINT+"."+Schema.COL_TRACK_ID+") FROM "+Schema.TBL_WAYPOINT+" WHERE "+Schema.TBL_WAYPOINT+"."+Schema.COL_TRACK_ID+" = " + Schema.TBL_TRACK + "." + Schema.COL_ID + ") as " + Schema.COL_WAYPOINT_COUNT
    };

    /**the group by statement that is used for the track statements*/
    private static final String TRACK_TABLES_GROUP_BY = Schema.TBL_TRACK + "." + Schema.COL_ID;

    /**Uri Matcher*/
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK, Schema.URI_CODE_TRACK);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/active", Schema.URI_CODE_TRACK_ACTIVE);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#", Schema.URI_CODE_TRACK_ID);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/start", Schema.URI_CODE_TRACK_START);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/end", Schema.URI_CODE_TRACK_END);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/" + Schema.TBL_WAYPOINT + "s", Schema.URI_CODE_TRACK_WAYPOINTS);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_TRACK + "/#/" + Schema.TBL_TRACKPOINT + "s", Schema.URI_CODE_TRACK_TRACKPOINTS);
        uriMatcher.addURI(AUTHORITY, Schema.TBL_WAYPOINT + "/uuid/*", Schema.URI_CODE_WAYPOINT_UUID);
    }

    /**
     * @param trackId target track id
     * @return Uri for the waypoints of the track
     */
    public static final Uri waypointsUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                Schema.TBL_WAYPOINT + "s" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the trackpoints of the track
     */
    public static final Uri trackPointsUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                Schema.TBL_TRACKPOINT + "s" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the startpoint of the track
     */
    public static final Uri trackStartUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                "start" );
    }

    /**
     * @param trackId target track id
     * @return Uri for the endpoint of the track
     */
    public static final Uri trackEndUri(long trackId) {
        return Uri.withAppendedPath(
                ContentUris.withAppendedId(CONTENT_URI_TRACK, trackId),
                "end" );
    }


    /**Database Helper*/
    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selectionIn, @Nullable String[] selectionArgsIn, @Nullable String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String selection = selectionIn;
        String[] selectionArgs = selectionArgsIn;

        String groupBy = null;
        String limit = null;

        // Select which datatype was requested
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_TRACKPOINTS:
                String trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_TRACKPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                // Deal with any additional selection info provided by the caller
                if (null != selectionIn) {
                    selection += " AND " + selectionIn;
                }

                List<String> selctionArgsList = new ArrayList<String>();
                selctionArgsList.add(trackId);
                // Add the callers selection arguments, if any
                if (null != selectionArgsIn) {
                    for (String arg : selectionArgsIn) {
                        selctionArgsList.add(arg);
                    }
                }
                selectionArgs = selctionArgsList.toArray(new String[0]);
                // Finished with the temporary selection arguments list. release it for GC
                selctionArgsList.clear();
                selctionArgsList = null;
                break;
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_WAYPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                selectionArgs = new String[] {trackId};
                break;
            case Schema.URI_CODE_TRACK_START:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_TRACKPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                selectionArgs = new String[] {trackId};
                sortOrder = Schema.COL_ID + " asc";
                limit = "1";
                break;
            case Schema.URI_CODE_TRACK_END:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getPathSegments().get(1);
                qb.setTables(Schema.TBL_TRACKPOINT);
                selection = Schema.COL_TRACK_ID + " = ?";
                selectionArgs = new String[] {trackId};
                sortOrder = Schema.COL_ID + " desc";
                limit = "1";
                break;
            case Schema.URI_CODE_TRACK:
                qb.setTables(TRACK_TABLES);
                if (projection == null)
                    projection = TRACK_TABLES_PROJECTION;
                groupBy = TRACK_TABLES_GROUP_BY;
                break;
            case Schema.URI_CODE_TRACK_ID:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                trackId = uri.getLastPathSegment();
                qb.setTables(TRACK_TABLES);
                if (projection == null)
                    projection = TRACK_TABLES_PROJECTION;
                groupBy = TRACK_TABLES_GROUP_BY;
                selection = Schema.TBL_TRACK + "." + Schema.COL_ID + " = ?";
                selectionArgs = new String[] {trackId};
                break;
            case Schema.URI_CODE_TRACK_ACTIVE:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                qb.setTables(Schema.TBL_TRACK);
                selection = Schema.COL_ACTIVE + " = ?";
                selectionArgs = new String[] {Integer.toString(Schema.VAL_TRACK_ACTIVE)};
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor c = qb.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, groupBy, null, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {


        // Select which type to return
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_TRACKPOINTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_TRACKPOINT;
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_WAYPOINT;
            case Schema.URI_CODE_TRACK:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + recopemValues.class.getPackage() + "."
                        + Schema.TBL_TRACK;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // Select which data type to insert
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_TRACKPOINTS:
                // Check that mandatory columns are present.
                if (values.containsKey(Schema.COL_TRACK_ID) && values.containsKey(Schema.COL_LONGITUDE)
                        && values.containsKey(Schema.COL_LATITUDE) && values.containsKey(Schema.COL_TIMESTAMP)) {

                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_TRACKPOINT, null, values);
                    if (rowId > 0) {
                        Uri trackpointUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(trackpointUri, null);
                        return trackpointUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_LONGITUDE + ", "
                            + Schema.COL_LATITUDE + ", " + Schema.COL_TIMESTAMP);
                }
                break;
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                // Check that mandatory columns are present.
                if (values.containsKey(Schema.COL_TRACK_ID) && values.containsKey(Schema.COL_LONGITUDE)
                        && values.containsKey(Schema.COL_LATITUDE) && values.containsKey(Schema.COL_TIMESTAMP) ) {

                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_WAYPOINT, null, values);
                    if (rowId > 0) {
                        Uri waypointUri = ContentUris.withAppendedId(uri, rowId);
                        getContext().getContentResolver().notifyChange(waypointUri, null);
                        return waypointUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_LONGITUDE + ", "
                            + Schema.COL_LATITUDE + ", " + Schema.COL_TIMESTAMP);
                }
                break;
            case Schema.URI_CODE_TRACK:
                if (values.containsKey(Schema.COL_START_DATE)) {
                    long rowId = dbHelper.getWritableDatabase().insert(Schema.TBL_TRACK, null, values);
                    if (rowId > 0) {
                        Uri trackUri = ContentUris.withAppendedId(CONTENT_URI_TRACK, rowId);
                        getContext().getContentResolver().notifyChange(trackUri, null);
                        return trackUri;
                    }
                } else {
                    throw new IllegalArgumentException("values should provide " + Schema.COL_START_DATE);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int count;
        // Select which data type to delete
        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK:
                count = dbHelper.getWritableDatabase().delete(Schema.TBL_TRACK, selection, selectionArgs);
                break;
            case Schema.URI_CODE_TRACK_ID:
                // the URI matches a specific track, delete all related entities
                String trackId = Long.toString(ContentUris.parseId(uri));
                dbHelper.getWritableDatabase().delete(Schema.TBL_WAYPOINT, Schema.COL_TRACK_ID + " = ?", new String[] {trackId});
                dbHelper.getWritableDatabase().delete(Schema.TBL_TRACKPOINT, Schema.COL_TRACK_ID + " = ?", new String[] {trackId});
                count = dbHelper.getWritableDatabase().delete(Schema.TBL_TRACK, Schema.COL_ID + " = ?", new String[] {trackId});
                break;
            case Schema.URI_CODE_WAYPOINT_UUID:
                String uuid = uri.getLastPathSegment();
                if(uuid != null){
                    count = dbHelper.getWritableDatabase().delete(Schema.TBL_WAYPOINT, Schema.COL_UUID + " = ?", new String[]{uuid});
                }else{
                    count = 0;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selectionIn, @Nullable String[] selectionArgsIn) {

        String table;
        String selection = selectionIn;
        String[] selectionArgs = selectionArgsIn;

        switch (uriMatcher.match(uri)) {
            case Schema.URI_CODE_TRACK_WAYPOINTS:
                if (selectionIn == null || selectionArgsIn == null) {
                    // Caller must narrow to a specific waypoint
                    throw new IllegalArgumentException();
                }
                table = Schema.TBL_WAYPOINT;
                break;
            case Schema.URI_CODE_TRACK_ID:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                table = Schema.TBL_TRACK;
                String trackId = uri.getLastPathSegment();
                selection = Schema.COL_ID + " = ?";
                selectionArgs = new String[] {trackId};
                break;
            case Schema.URI_CODE_TRACK_ACTIVE:
                if (selectionIn != null || selectionArgsIn != null) {
                    // Any selection/selectionArgs will be ignored
                    throw new UnsupportedOperationException();
                }
                table = Schema.TBL_TRACK;
                selection = Schema.COL_ACTIVE + " = ?";
                selectionArgs = new String[] {Integer.toString(Schema.VAL_TRACK_ACTIVE)};
                break;
            case Schema.URI_CODE_TRACK:
                // Dangerous: Will update all the tracks, but necessary for instance
                // to switch all the tracks to inactive
                table = Schema.TBL_TRACK;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int rows = dbHelper.getWritableDatabase().update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }


    /**
     * Represents Data Schema.
     */
    public static final class Schema {
        public static final String TBL_TRACKPOINT = "trackpoint";
        public static final String TBL_WAYPOINT = "waypoint";
        public static final String TBL_TRACK = "track";

        public static final String COL_ID = "_id";
        public static final String COL_TRACK_ID = "track_id";
        public static final String COL_UUID = "uuid";
        public static final String COL_LONGITUDE = "longitude";
        public static final String COL_LATITUDE = "latitude";
        public static final String COL_SPEED = "speed";
        public static final String COL_ACCURACY = "accuracy";
        public static final String COL_TIMESTAMP = "point_timestamp";
        public static final String COL_NAME = "name";
        public static final String COL_START_DATE = "start_date";

        //Specific to Jean
        public static final String COL_PIC_PATH ="path_to_pictures";
        public static final String COL_INF_ID = "Inf_ID";
        public static final String RECOPEM_TRACK_ID = "Track_ID";
        public static final String COL_GPS_METHOD = "GPS_data_coll_method";
        public static final String COL_GPS_COMMENTS = "GPS_comments";
        public static final String COL_WEEKDAY = "Weekday";
        public static final String COL_DAY_NIGHT = "Day_night";
        public static final String COL_GEAR = "Gear";
        public static final String COL_GEAR_DETAILS = "Gear_details";
        public static final String COL_TRANSPORT = "Transport_to_landing";
        public static final String COL_HOUR_START = "Hour_st";
        public static final String COL_HOUR_END = "Hour_end";
        public static final String COL_BOAT = "Boat";
        public static final String COL_BOAT_OWNER = "Boat_owner";
        public static final String COL_CREW ="Crew";
        public static final String COL_WEATHER_COMMENTS ="Weather_comments";
        public static final String COL_WIND_FISHER = "Wind_est_fisher";
        public static final String COL_SWELL_FISHER = "Current_est_fisher";
        public static final String COL_TARGET_SPECIES = "Target_species";
        public static final String COL_CATCH_TOTAL = "Catch_total";
        public static final String COL_CATCH_N_FISHER = "N_fisher_share_catch";
        public static final String COL_CATCH_SALE = "Catch_sale";
        public static final String COL_CATCH_ORDER ="Catch_order";
        public static final String COL_CATCH_CONS = "Catch_cons";
        public static final String COL_CATCH_GIVE = "Catch_give";
        public static final String COL_PIC_NB = "Picture_nb";
        public static final String COL_PIC_SCALE = "Picture_scale";
        public static final String COL_PIC_COMMENTS = "Picture_comments";
        public static final String COL_WG_WIND = "WG_wind_speed_knots";
        public static final String COL_WG_WIND_DIR =" WG_wind_dir";
        public static final String COL_WG_SWELL_M = "WG_swell_m";
        public static final String COL_WG_SWELL_DIR = "WG_swell_dir";
        public static final String COL_WG_SWELL_PERIOD = "WG_swell_period";
        public static final String COL_WG_TEMP = "WG_temp";
        public static final String COL_MOON = "Moon";
        public static final String COL_MOON_RISE = "Moon_rise";
        public static final String COL_MOON_SET = "Moon_set";

        @Deprecated
        //public static final String COL_DIR = "directory";
        public static final String COL_ACTIVE = "active";

        // virtual colums that are used in some sqls but dont exist in database
        public static final String COL_TRACKPOINT_COUNT = "tp_count";
        public static final String COL_WAYPOINT_COUNT = "wp_count";

        // Codes for UriMatcher
        public static final int URI_CODE_TRACK = 3;
        public static final int URI_CODE_TRACK_ID = 4;
        public static final int URI_CODE_TRACK_WAYPOINTS = 5;
        public static final int URI_CODE_TRACK_TRACKPOINTS = 6;
        public static final int URI_CODE_TRACK_ACTIVE = 7;
        public static final int URI_CODE_WAYPOINT_UUID = 8;
        public static final int URI_CODE_TRACK_START = 9;
        public static final int URI_CODE_TRACK_END = 10;

        public static final int VAL_TRACK_ACTIVE = 1;
        public static final int VAL_TRACK_INACTIVE = 0;
    }
}
