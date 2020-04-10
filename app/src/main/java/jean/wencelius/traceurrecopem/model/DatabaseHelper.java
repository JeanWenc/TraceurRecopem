package jean.wencelius.traceurrecopem.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import jean.wencelius.traceurrecopem.recopemValues;

/**
 * Created by Jean Wencélius on 09/04/2020.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DB_NAME = recopemValues.class.getSimpleName();
    private static final int DB_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**SQL for creating table TRACKPOINT*/
    private static final String SQL_CREATE_TABLE_TRACKPOINT = ""
            + "create table " + TrackContentProvider.Schema.TBL_TRACKPOINT + " ("
            + TrackContentProvider.Schema.COL_ID	+ " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_LATITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_LONGITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_SPEED + " double null,"
            + TrackContentProvider.Schema.COL_ACCURACY + " double null,"
            + TrackContentProvider.Schema.COL_TIMESTAMP + " long not null,"+ ")";

    /**SQL for creating index TRACKPOINT_idx (track id)*/
    private static final String SQL_CREATE_IDX_TRACKPOINT_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_TRACKPOINT
            + "_idx ON " + TrackContentProvider.Schema.TBL_TRACKPOINT + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";

    /**SQL for creating table WAYPOINT*/
    private static final String SQL_CREATE_TABLE_WAYPOINT = ""
            + "create table " + TrackContentProvider.Schema.TBL_WAYPOINT + " ("
            + TrackContentProvider.Schema.COL_ID + " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_TRACK_ID + " integer not null,"
            + TrackContentProvider.Schema.COL_UUID + " text,"
            + TrackContentProvider.Schema.COL_LATITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_LONGITUDE + " double not null,"
            + TrackContentProvider.Schema.COL_ACCURACY + " double null,"
            + TrackContentProvider.Schema.COL_TIMESTAMP + " long not null,"
            + TrackContentProvider.Schema.COL_NAME + " text," + ")";

    /**SQL for creating index WAYPOINT_idx (track id)*/
    private static final String SQL_CREATE_IDX_WAYPOINT_TRACK
            = "create index if not exists "
            + TrackContentProvider.Schema.TBL_WAYPOINT
            + "_idx ON " + TrackContentProvider.Schema.TBL_WAYPOINT + "(" + TrackContentProvider.Schema.COL_TRACK_ID + ")";

    /**SQL for creating table TRACK*/
    @SuppressWarnings("deprecation")
    private static final String SQL_CREATE_TABLE_TRACK = ""
            + "create table " + TrackContentProvider.Schema.TBL_TRACK + " ("
            + TrackContentProvider.Schema.COL_ID + " integer primary key autoincrement,"
            + TrackContentProvider.Schema.COL_INF_ID + " text,"
            + TrackContentProvider.Schema.RECOPEM_TRACK_ID + " text,"
            + TrackContentProvider.Schema.COL_GPS_METHOD + " text,"
            + TrackContentProvider.Schema.COL_GPS_COMMENTS + " text,"
            + TrackContentProvider.Schema.COL_START_DATE + " long not null,"
            + TrackContentProvider.Schema.COL_WEEKDAY + " text,"
            + TrackContentProvider.Schema.COL_DAY_NIGHT + " text,"
            + TrackContentProvider.Schema.COL_GEAR + " text,"
            + TrackContentProvider.Schema.COL_GEAR_DETAILS + " text,"
            + TrackContentProvider.Schema.COL_TRANSPORT + " text,"
            + TrackContentProvider.Schema.COL_HOUR_START + " long,"
            + TrackContentProvider.Schema.COL_HOUR_END + " long,"
            + TrackContentProvider.Schema.COL_BOAT + " text,"
            + TrackContentProvider.Schema.COL_BOAT_OWNER + " text,"
            + TrackContentProvider.Schema.COL_CREW + " text,"
            + TrackContentProvider.Schema.COL_WEATHER_COMMENTS + " text,"
            + TrackContentProvider.Schema.COL_WIND_FISHER + " text,"
            + TrackContentProvider.Schema.COL_SWELL_FISHER + " text,"
            + TrackContentProvider.Schema.COL_TARGET_SPECIES + " text,"
            + TrackContentProvider.Schema.COL_CATCH_TOTAL + " text,"
            + TrackContentProvider.Schema.COL_CATCH_N_FISHER + " text,"
            + TrackContentProvider.Schema.COL_CATCH_SALE + " text,"
            + TrackContentProvider.Schema.COL_CATCH_ORDER + " text,"
            + TrackContentProvider.Schema.COL_CATCH_CONS + " text,"
            + TrackContentProvider.Schema.COL_CATCH_GIVE + " text,"
            + TrackContentProvider.Schema.COL_PIC_NB + " text,"
            + TrackContentProvider.Schema.COL_PIC_SCALE + " text,"
            + TrackContentProvider.Schema.COL_PIC_COMMENTS + " text,"
            + TrackContentProvider.Schema.COL_WG_WIND + " text,"
            + TrackContentProvider.Schema.COL_WG_WIND_DIR + " text,"
            + TrackContentProvider.Schema.COL_WG_SWELL_M + " text,"
            + TrackContentProvider.Schema.COL_WG_SWELL_DIR + " text,"
            + TrackContentProvider.Schema.COL_WG_SWELL_PERIOD + " text,"
            + TrackContentProvider.Schema.COL_WG_TEMP + " text,"
            + TrackContentProvider.Schema.COL_MOON + " text,"
            + TrackContentProvider.Schema.COL_MOON_RISE + " text,"
            + TrackContentProvider.Schema.COL_MOON_SET + " text,"
            + TrackContentProvider.Schema.RECOPEM_TRACK_ID + " text,"
            + TrackContentProvider.Schema.COL_ACTIVE + " integer not null default 0,"+ ")";
//+ TrackContentProvider.Schema.COL_DIR + " text," // unused since DB_VERSION 13, since SQLite doesn't support to remove a column it will stay for now

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_TRACKPOINT);
        db.execSQL(SQL_CREATE_TABLE_TRACKPOINT);
        db.execSQL(SQL_CREATE_IDX_TRACKPOINT_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_WAYPOINT);
        db.execSQL(SQL_CREATE_TABLE_WAYPOINT);
        db.execSQL(SQL_CREATE_IDX_WAYPOINT_TRACK);
        db.execSQL("drop table if exists " + TrackContentProvider.Schema.TBL_TRACK);
        db.execSQL(SQL_CREATE_TABLE_TRACK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
