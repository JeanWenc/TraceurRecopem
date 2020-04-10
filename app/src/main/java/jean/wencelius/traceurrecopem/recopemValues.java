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
}
