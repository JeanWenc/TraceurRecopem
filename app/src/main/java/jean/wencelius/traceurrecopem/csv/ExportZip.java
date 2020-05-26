package jean.wencelius.traceurrecopem.csv;

import android.content.Context;

/**
 * Created by Jean Wencélius on 18/05/2020.
 */
public class ExportZip extends CreateZipTask {
    //TODO: Status of emailSent in TrackDetailActivity not satisfying because appears as true even if action cancelled by user.
    public ExportZip(Context context, String saveDir) {
        super(context, saveDir);
    }



    /*static final int BUFFER = 2048;

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
    }*/
}
