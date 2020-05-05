package jean.wencelius.traceurrecopem;

import android.os.Environment;

import jean.wencelius.traceurrecopem.utils.BeaconOverlay;

/**
 * Created by Jean Wencélius on 09/04/2020.
 */
public class recopemValues {
    /**
     * The full Package name of OSMTracker returned by calling
     * OSMTracker.class.getPackage().getName()
     */
    public final static String PACKAGE_NAME = recopemValues.class.getPackage().getName();

    /**
     * Intent for tracking a waypoint
     */
    public final static String INTENT_TRACK_WP = recopemValues.PACKAGE_NAME + ".intent.TRACK_WP";

    /**
     * Intent to start tracking
     */
    public final static String INTENT_START_TRACKING = recopemValues.PACKAGE_NAME + ".intent.START_TRACKING";

    /**
     * Intent to stop tracking
     */
    public final static String INTENT_STOP_TRACKING = recopemValues.PACKAGE_NAME + ".intent.STOP_TRACKING";

    /**JW: All the below is when creating a waypoint*/
    /**
     * Key for extra data "waypoint name" in Intent
     */
    public final static String INTENT_KEY_NAME = "name";

    /**
     * Key for extra data "uuid" in Intent
     */
    public final static String INTENT_KEY_UUID = "uuid";

    public final static String KEY_STORAGE_DIR = "logging.storage.dir";
    public final static String VAL_STORAGE_DIR = "RecopemTracks";

    public final static String KEY_OUTPUT_FILENAME = "gpx.filename";
    public final static String VAL_OUTPUT_FILENAME_NAME = "name";

    /** Device string identifiers */
    public static final class Devices {
        public static final String NEXUS_S = "Nexus S";
    }

    public static String getWeekdayString (int intWeekday){
        String mWeekdayString = new String();
        switch (intWeekday){
            case 1:
                mWeekdayString = "Dimanche";
                break;
            case 2 :
                mWeekdayString = "Lundi";
                break;
            case 3:
                mWeekdayString = "Mardi";
                break;
            case 4:
                mWeekdayString = "Mercredi";
                break;
            case 5:
                mWeekdayString = "Jeudi";
                break;
            case 6:
                mWeekdayString = "Vendredi";
                break;
            case 7:
                mWeekdayString = "Samedi";
                break;
        }
        return mWeekdayString;
    }
}
