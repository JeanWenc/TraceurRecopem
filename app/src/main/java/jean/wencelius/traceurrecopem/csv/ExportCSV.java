package jean.wencelius.traceurrecopem.csv;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.opencsv.CSVWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.DataHelper;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.exception.ExportTrackException;
import jean.wencelius.traceurrecopem.recopemValues;

/**
 * Created by Jean Wencélius on 18/05/2020.
 */
public class ExportCSV {
    static final int BUFFER = 2048;

    public static void exportCSV(Context context, Long trackId, String saveDir, String exportType){

        File sdRoot = Environment.getExternalStorageDirectory();
        ContentResolver cr = context.getContentResolver();
        Uri trackUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, trackId);
        Cursor cursor = null;

        //JW: For file name
        String startDateYearMonthDay = saveDir.substring(saveDir.length()-19);
        startDateYearMonthDay = startDateYearMonthDay.substring(0,10);

        String filenameBase="";

        if(exportType.equals(recopemValues.EXPORT_TRACK_DATA)){
            filenameBase= startDateYearMonthDay + DataHelper.EXTENSION_CSV;
            //Which column you want to exprort
            cursor = cr.query(trackUri, null, null, null, null);
        }else if(exportType.equals(recopemValues.EXPORT_CAUGHT_FISH)){
            filenameBase= startDateYearMonthDay +"_fish_caught"+ DataHelper.EXTENSION_CSV;
            //Which column you want to exprort
            cursor = cr.query(Uri.withAppendedPath(trackUri, TrackContentProvider.Schema.TBL_POISSON + "s"), null, null, null, null);
        }

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (sdRoot.canWrite()) {
                File trackCSVExportDirectory = new File(saveDir);

                File fileToExport = new File(trackCSVExportDirectory, filenameBase);

                boolean goExport = false;
                if(null!=cursor) goExport = cursor.getCount()>0;

                if (goExport) {
                    try {
                        fileToExport.createNewFile();
                        CSVWriter csvWrite = new CSVWriter(new FileWriter(fileToExport));
                        csvWrite.writeNext(cursor.getColumnNames());
                        while (cursor.moveToNext()) {
                            if (exportType.equals(recopemValues.EXPORT_TRACK_DATA)) {
                                String trackArrStr[] ={
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ACTIVE))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DIR)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_NAME)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_RECOPEM_TRACK_ID)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_EXPORTED)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_PIC_ADDED)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_START_DATE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_GPS_METHOD)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_WEEKDAY)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_DEVICE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_GEAR)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_GEAR_OTHER_DETAILS)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_BOAT)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_BOAT_OWNER)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_ALONE)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_N))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CREW_WHO)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_WIND_FISHER)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CURRENT_FISHER)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_N))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_TYPE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_PRICE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_WHERE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_DETAILS)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_SALE_PIC)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_N))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_TYPE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_PRICE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_WHERE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_DETAILS)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_ORDER_PIC)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_N))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_TYPE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_WHERE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_DETAILS)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_GIVE_PIC)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_N))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_TYPE)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_DETAILS)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_CONS_PIC)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACKPOINT_COUNT))),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_WAYPOINT_COUNT)))
                                };
                                csvWrite.writeNext(trackArrStr);
                            } else if (exportType.equals(recopemValues.EXPORT_CAUGHT_FISH)) {

                                String fishArrStr [] = {
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_ID))),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_TRACK_ID))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_DESTINATION)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISH_FAMILY)),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_FISH_TAHITIAN)),
                                        Integer.toString(cursor.getInt(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N))),
                                        cursor.getString(cursor.getColumnIndex(TrackContentProvider.Schema.COL_CATCH_N_TYPE))
                                };

                                csvWrite.writeNext(fishArrStr);
                            }
                        }
                        csvWrite.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Toast.makeText(context, Integer.toString(cursor.getCount()), Toast.LENGTH_SHORT).show();
        cursor.close();
    }

    public static void zip(String saveDir){

        File zipExportDirectory = new File(saveDir);

        //JW: For file name
        String startDateYearMonthDay = saveDir.substring(saveDir.length()-19);
        startDateYearMonthDay = startDateYearMonthDay.substring(0,10);

        File fileList[] = zipExportDirectory.listFiles();

        int file_size = 0;
        for(int i=0; i < fileList.length; i++){
            file_size+= Integer.parseInt(String.valueOf(fileList[i].length()/1024));
        }

        double n_zip_temp = file_size/10000;
        int n_zip = (int) Math.floor(n_zip_temp)+1;

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
                    String iFile = fileList[fileCount].getAbsolutePath();
                    jFileSize+=Integer.parseInt(String.valueOf(fileList[fileCount].length()/1024));
                    if(jFileSize<10000){
                        FileInputStream fi = new FileInputStream(iFile);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(iFile.substring(iFile.lastIndexOf("/") + 1));
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                        fileCount++;
                    }else{
                        fileCount=fileCount-1;
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
