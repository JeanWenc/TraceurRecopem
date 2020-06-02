package jean.wencelius.traceurrecopem.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.model.AppPreferences;
import jean.wencelius.traceurrecopem.recopemValues;
import jean.wencelius.traceurrecopem.utils.DatePickerFragment;
import jean.wencelius.traceurrecopem.utils.TimePickerFragment;

public class ManualTrackActivity extends AppCompatActivity{

    private Button mNxtBtn;

    private TextView mDayText;
    private TextView mDepTimeText;
    private TextView mArrTimeText;

    private long mNewTrackId;

    private String mDay;
    private String mDepTime;
    private String mArrTime;

    private String mTimeSelBtn;
    private String mDayOfWeek;

    private Calendar mDayCal;

    private static final String BUNDLE_STATE_DAY="day";
    private static final String BUNDLE_STATE_DEP_TIME="depTime";
    private static final String BUNDLE_STATE_ARR_TIME="arrTime";

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private final SimpleDateFormat time = new SimpleDateFormat("HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_track);

        setTitle(R.string.activity_manual_track_title);

        mNxtBtn = (Button) findViewById(R.id.activity_manual_track_next_btn);

        mDayText = (TextView) findViewById(R.id.activity_manual_track_date_input_text);
        mDepTimeText = (TextView) findViewById(R.id.activity_manual_track_dep_time_input_text);
        mArrTimeText = (TextView) findViewById(R.id.activity_manual_track_arr_time_input_text);

        mTimeSelBtn = "";

        mNewTrackId = -1;
        if(savedInstanceState!=null){
            mNxtBtn.setEnabled(savedInstanceState.getBoolean(recopemValues.BUNDLE_STATE_BUTTON));
            mDay = savedInstanceState.getString(BUNDLE_STATE_DAY);
            mDepTime = savedInstanceState.getString(BUNDLE_STATE_DEP_TIME);
            mArrTime =savedInstanceState.getString(BUNDLE_STATE_ARR_TIME);

        }else{
            mNxtBtn.setEnabled(false);
            mDay = "";
            mDepTime = "";
            mArrTime = "";
        }
        mDayText.setText(mDay);
        mDepTimeText.setText(mDepTime);
        mArrTimeText.setText(mArrTime);

        mNxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckArrDepTimes();
                FillInDB();

                Intent MapAloneActivity = new Intent(ManualTrackActivity.this, MapAloneActivity.class);
                MapAloneActivity.putExtra(recopemValues.BUNDLE_EXTRA_CREATE_MANUAL_TRACK,"true");
                MapAloneActivity.putExtra(recopemValues.BUNDLE_EXTRA_CREATE_MANUAL_TRACK_ID,mNewTrackId);
                startActivity(MapAloneActivity);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(recopemValues.BUNDLE_STATE_BUTTON,mNxtBtn.isEnabled());
        outState.putString(BUNDLE_STATE_DAY,mDay);
        outState.putString(BUNDLE_STATE_DEP_TIME,mDepTime);
        outState.putString(BUNDLE_STATE_ARR_TIME,mArrTime);
        super.onSaveInstanceState(outState);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
        if(v.getId()==R.id.activity_manual_track_dep_time_btn) mTimeSelBtn = BUNDLE_STATE_DEP_TIME;
        if(v.getId()==R.id.activity_manual_track_arr_time_btn) mTimeSelBtn = BUNDLE_STATE_ARR_TIME;
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setTime(int hour, int minute) {
        String strHour = Integer.toString(hour);
        String strMin = Integer.toString(minute);
        if(strHour.length()==1) strHour = "0"+strHour;
        if(strMin.length()==1) strMin = "0"+strMin;

        String time = strHour+"-"+strMin+"-00";
        String timeText = strHour+":"+strMin;
        if(mTimeSelBtn.equals(BUNDLE_STATE_DEP_TIME)){
            mDepTime = time;
            mDepTimeText.setText(timeText);
        }else if(mTimeSelBtn.equals(BUNDLE_STATE_ARR_TIME)){
            mArrTime = time;
            mArrTimeText.setText(timeText);
        }
        mNxtBtn.setEnabled(!mDay.equals("") && !mDepTime.equals("") && !mArrTime.equals(""));
    }

    public void setDate(String strDate){
        mDayCal = Calendar.getInstance();
        mDayOfWeek = "";
        try {
            mDayCal.setTime(sdf.parse(strDate));
            mDayOfWeek = recopemValues.getWeekdayString(mDayCal.get(Calendar.DAY_OF_WEEK));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mDay = strDate;
        mDayText.setText(mDayOfWeek + " - " +mDay);
        mNxtBtn.setEnabled(!mDay.equals("") && !mDepTime.equals("") && !mArrTime.equals(""));
    }

    private void CheckArrDepTimes(){


       int hourDep = Integer.parseInt(mDepTime.substring(0,2));
       int hourArr = Integer.parseInt(mArrTime.substring(0,2));;

       boolean addDay = false;

        if(hourArr<hourDep){
            addDay=true;
        }else if (hourArr==hourDep) {
            int minDep = Integer.parseInt(mDepTime.substring(3,5));
            int minArr = Integer.parseInt(mArrTime.substring(3,5));
            if (minArr < minDep) addDay = true;
        }

        mDepTime = mDay+"_"+ mDepTime;

        if(addDay){
            mDayCal.add(Calendar.DATE,1);
            mDay = sdf.format(mDayCal.getTime());
        }
        mArrTime = mDay+"_"+mArrTime;
    }

    private void FillInDB() {

        Date startDate = null;
        try {
            startDate = sdfTime.parse(mDepTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(startDate!=null){
            String saveDirectory = MenuActivity.getDataTrackDirectory(startDate);
            String fisherId = AppPreferences.getDefaultsString(recopemValues.PREF_KEY_FISHER_ID,getApplicationContext());

            String mRecopemId = fisherId + "_" + mDay;

            // Create entry in TRACK table
            ContentValues values = new ContentValues();
            values.put(TrackContentProvider.Schema.COL_INF_ID, fisherId);
            values.put(TrackContentProvider.Schema.COL_START_DATE, startDate.getTime());
            values.put(TrackContentProvider.Schema.COL_HOUR_START,mDepTime);
            values.put(TrackContentProvider.Schema.COL_HOUR_END,mArrTime);
            values.put(TrackContentProvider.Schema.COL_RECOPEM_TRACK_ID, mRecopemId);
            values.put(TrackContentProvider.Schema.COL_GPS_METHOD,"Manual");
            values.put(TrackContentProvider.Schema.COL_WEEKDAY,mDayOfWeek);
            values.put(TrackContentProvider.Schema.COL_TRACK_DATA_ADDED,"false");
            values.put(TrackContentProvider.Schema.COL_PIC_ADDED,"false"); // other value should be "true"
            values.put(TrackContentProvider.Schema.COL_CAUGHT_FISH_DETAILS,"false");
            values.put(TrackContentProvider.Schema.COL_EXPORTED,"false");
            values.put(TrackContentProvider.Schema.COL_SENT_EMAIL,"false");
            values.put(TrackContentProvider.Schema.COL_DIR,saveDirectory);
            values.put(TrackContentProvider.Schema.COL_DEVICE,android.os.Build.MODEL);
            values.put(TrackContentProvider.Schema.COL_ACTIVE, TrackContentProvider.Schema.VAL_TRACK_INACTIVE);

            Uri trackUri = getContentResolver().insert(TrackContentProvider.CONTENT_URI_TRACK, values);

            mNewTrackId = ContentUris.parseId(trackUri);

            String defImage = "android.resource://jean.wencelius.traceurrecopem/drawable/add_picture";

            ContentValues picVal = new ContentValues();
            picVal.put(TrackContentProvider.Schema.COL_TRACK_ID,mNewTrackId);
            picVal.put(TrackContentProvider.Schema.COL_PIC_PATH,defImage);

            Uri picUri = ContentUris.withAppendedId(TrackContentProvider.CONTENT_URI_TRACK, mNewTrackId);
            getContentResolver().insert(Uri.withAppendedPath(picUri, TrackContentProvider.Schema.TBL_PICTURE + "s"), picVal);
        }
    }
}
