package jean.wencelius.traceurrecopem.gpx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.Date;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.exception.ExportTrackException;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.recopemValues;

/**
 * Created by Jean Wencélius on 09/04/2020.
 */
public class ExportToStorageTask extends ExportTrackTask {

    private static final String TAG = ExportToStorageTask.class.getSimpleName();

    public ExportToStorageTask(Context context, long... trackId) {
        super(context, trackId);
    }

    @Override
    protected File getExportDirectory(Date startDate) throws ExportTrackException {
        File sdRoot = Environment.getExternalStorageDirectory();

        // The location that the user has specified gpx files
        // and associated content to be written
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userGPXExportDirectoryName = prefs.getString(
                recopemValues.KEY_STORAGE_DIR,	recopemValues.VAL_STORAGE_DIR);

        // Create the path to the directory to which we will be writing
        // Trim the directory name, as additional spaces at the end will
        // not allow the directory to be created if required
        String exportDirectoryPath = userGPXExportDirectoryName.trim();
        String perTrackDirectory = File.separator + DataHelper.FILENAME_FORMATTER.format(startDate);

        // Create a file based on the path we've generated above
        File trackGPXExportDirectory = new File(sdRoot + exportDirectoryPath + perTrackDirectory);

        // Create track directory if needed
        if (! trackGPXExportDirectory.exists()) {
            if (! trackGPXExportDirectory.mkdirs()) {
                Log.w(TAG,"Failed to create directory ["
                        +trackGPXExportDirectory.getAbsolutePath()+ "]");
            }

            if (! trackGPXExportDirectory.exists()) {
                // Specific hack for Google Nexus  S(See issue #168)
                if (android.os.Build.MODEL.equals(recopemValues.Devices.NEXUS_S)) {
                    // exportDirectoryPath always starts with "/"
                    trackGPXExportDirectory = new File(exportDirectoryPath + perTrackDirectory);
                    trackGPXExportDirectory.mkdirs();
                }
            }

            if (! trackGPXExportDirectory.exists()) {
                throw new ExportTrackException(context.getResources().getString(R.string.error_create_track_dir,
                        trackGPXExportDirectory.getAbsolutePath()));
            }
        }
        return trackGPXExportDirectory;
    }

    @Override
    protected boolean exportMediaFiles() {
        return false;
    }

    @Override
    protected boolean updateExportDate() {
        return false;
    }
}
