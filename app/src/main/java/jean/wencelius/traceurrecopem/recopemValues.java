package jean.wencelius.traceurrecopem;

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

    public static final String PREF_KEY_FISHER_NAME = "PREF_KEY_FISHER_NAME";
    public static final String PREF_KEY_FISHER_ID = "PREF_KEY_FISHER_ID";
    public static final String PREF_KEY_FISHER_BOAT = "PREF_KEY_FISHER_BOAT";
    public static final String PREF_KEY_FISHER_BOAT_OWNER = "PREF_KEY_FISHER_BOAT_OWNER";
    public static final String PREF_KEY_FISHER_LOCATION_SALE_PREF = "PREF_KEY_FISHER_LOCATION_SALE_PREF";

    public static final String BUNDLE_STATE_ANS = "mainAnswer";
    public static final String BUNDLE_STATE_CATCH_N = "catchN";
    public static final String BUNDLE_STATE_TYPE_INT = "typeInt";
    public static final String BUNDLE_STATE_PRICE_INT = "priceInt";
    public static final String BUNDLE_STATE_WHERE_INT = "whereInt";
    public static final String BUNDLE_STATE_DETAILS = "details";
    public static final String BUNDLE_STATE_PIC_ANS = "picAnswer";
    public static final String BUNDLE_STATE_BUTTON = "nxtButton";
    public static final String BUNDLE_STATE_TRACK_ID = "trackId";
    public static final String BUNDLE_STATE_NEW_PIC_ADDED = "newPicAdded";
    public static final String BUNDLE_EXTRA_CATCH_DESTINATION = "catchDestination";

    public static final String BUNDLE_STATE_SALE_PIC_ANS = "salePicAnswer";
    public static final String BUNDLE_STATE_ORDER_PIC_ANS = "orderPicAnswer";
    public static final String BUNDLE_STATE_GIVE_PIC_ANS = "givePicAnswer";

    public static final String EXPORT_TRACK_DATA = "exportTrackData";
    public static final String EXPORT_CAUGHT_FISH = "exportCaughtFish";

    public static final String EMAIL_RECIPIENT = "jeanwencelius@gmail.com";

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
