package jean.wencelius.traceurrecopem.csv;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.exception.ExportTrackException;

/**
 * Created by Jean Wencélius on 18/05/2020.
 */
public abstract class CreateZipTask extends AsyncTask<Void, Long, Boolean> {

    protected String saveDir;

    protected ProgressDialog dialog;

    protected Context context;

    protected File zipExportDirectory;

    protected File[] fileList;

    private String errorMsg = null;

    //protected abstract File getExportDirectory(String startDate) throws ExportTrackException;

    static final int BUFFER = 2048;
    static final int ZIP_MAX_SIZE = 10000;

    public CreateZipTask(Context context, String saveDir) {
        this.saveDir = saveDir;
        this.context = context;

        this.zipExportDirectory = new File(saveDir);
        this.fileList = zipExportDirectory.listFiles();
    }

    @Override
    protected void onPreExecute() {
        // Display dialog
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setProgress(0);
        dialog.setMax(fileList.length);
        dialog.setMessage(context.getResources().getString(R.string.activity_track_detail_export_msg));
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        zip(saveDir);
        return true;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        dialog.incrementProgressBy(values[0].intValue());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        dialog.dismiss();
        if (!success) {
            new AlertDialog.Builder(context)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(context.getResources()
                            .getString(R.string.activity_track_detail_zip_task_msg_error))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void zip(String saveDir){

        //JW: For file name
        String startDateYearMonthDay = saveDir.substring(saveDir.length()-19);
        startDateYearMonthDay = startDateYearMonthDay.substring(0,10);

        //Caculate total file size and determine number of needed zip archive to stay under 10000
        int file_size = 0;
        for(int i=0; i < fileList.length; i++){
            file_size+= Integer.parseInt(String.valueOf(fileList[i].length()/1024));
        }
        int n_zip = (int) Math.floor(file_size/ZIP_MAX_SIZE)+1;


        File [] _zipFile = new File[n_zip];
        for(int i=0;i<n_zip;i++){
            _zipFile[i] = new File(zipExportDirectory,startDateYearMonthDay + Integer.toString(i) + DataHelper.EXTENSION_ZIP);
        }

        int fileCount = 0;

        for(int j =0; j<n_zip;j++){
            try  {
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream(_zipFile[j]);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

                byte data[] = new byte[BUFFER];

                int jFileSize = 0;

                while(fileCount < fileList.length) {
                    publishProgress((long) fileCount);
                    String iFile = fileList[fileCount].getAbsolutePath();
                    jFileSize+=Integer.parseInt(String.valueOf(fileList[fileCount].length()/1024));
                    if(jFileSize<ZIP_MAX_SIZE){
                        FileInputStream fi = new FileInputStream(iFile);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(iFile.substring(iFile.lastIndexOf("/") + 1));
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                        fileCount++;
                        origin.close();
                    }else{
                        break;
                    }
                }
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
