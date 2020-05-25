package jean.wencelius.traceurrecopem.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jean.wencelius.traceurrecopem.controller.ManualTrackActivity;
import jean.wencelius.traceurrecopem.controller.dataInput.dataInputFishCaught;

/**
 * Created by Jean Wencélius on 22/05/2020.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String strYear = Integer.toString(year);
        String strMonth = Integer.toString(month+1);
        if(strMonth.length()==1) strMonth = "0"+strMonth;
        String strDay = Integer.toString(dayOfMonth);
        if(strDay.length()==1) strDay = "0"+strDay;

        String dateString = strYear+"-"+strMonth+"-"+strDay;
        ManualTrackActivity prevActivity = (ManualTrackActivity) getActivity();
        prevActivity.setDate(dateString);
    }
}
