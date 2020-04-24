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

    public ExportToStorageTask(Context context, String saveDir, long... trackId) {
        super(context, saveDir,trackId);
    }

    @Override
    protected File getExportDirectory(String saveDir) throws ExportTrackException {

        File trackGPXExportDirectory = new File(saveDir);

        // Create track directory if needed
        if (! trackGPXExportDirectory.exists()) {
            if (! trackGPXExportDirectory.mkdirs()) {
                Log.w(TAG,"Failed to create directory ["
                        +trackGPXExportDirectory.getAbsolutePath()+ "]");
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
