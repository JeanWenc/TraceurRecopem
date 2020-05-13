package jean.wencelius.traceurrecopem.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.controller.dataInput.dataInputCatchSale;
import jean.wencelius.traceurrecopem.controller.dataInput.dataInputFishCaught;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FishPickerDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FishPickerDialog extends DialogFragment implements NumberPicker.OnValueChangeListener {

    //TODO: in Dialog do cursorOfFishCatch (trackId + mCatchDestination)
    //TODO: Before populating cursor make sure value doesn't already exist. If it does warn user he is changing values
    //TODO: This method can then be applied to all the dataInput methods to check for any changes (on a track curso and not a fishCatch Cursor)

    private static final String ARG_FISH_FAMILY = "fishFamily";
    private static final String ARG_FISH_TAHITIAN = "fishTahitian";
    private static final String ARG_CATCH_DESTINATION = "catchDestination";
    private static final String ARG_TRACK_ID = "trackId";
    public static final String ARG_IN_PICTURES = "inPictures";

    private String [] type;
    public ContentResolver mCr;
    public Cursor mCursorFishCaught;

    private String mFishFamily;
    private String mFishTahitian;
    private long mTrackId;
    private String mCatchDestination;
    private String mInPictures;

    private TextView mDialogFishName;
    private Button mOkButton;
    private Button mCancelButton;

    private NumberPicker mPickerCatchN;
    private int mCatchN;

    private NumberPicker mPickerCatchType;
    private int mCatchTypeInt;
    private String mCatchType;

    public FishPickerDialog() {
        // Required empty public constructor
    }

    public static FishPickerDialog newInstance(String fishFamily, String fishTahitian, long trackId, String catchDestination, String inPictures) {
        FishPickerDialog fragment = new FishPickerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_FISH_FAMILY, fishFamily);
        args.putString(ARG_FISH_TAHITIAN, fishTahitian);
        args.putLong(ARG_TRACK_ID, trackId);
        args.putString(ARG_CATCH_DESTINATION, catchDestination);
        args.putString(ARG_IN_PICTURES, inPictures);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFishFamily = getArguments().getString(ARG_FISH_FAMILY);
            mFishTahitian = getArguments().getString(ARG_FISH_TAHITIAN);
            mTrackId = getArguments().getLong(ARG_TRACK_ID);
            mCatchDestination = getArguments().getString(ARG_CATCH_DESTINATION);
            mInPictures = getArguments().getString(ARG_IN_PICTURES);

            mCatchN = 0;
            mCatchTypeInt = 0;
            mCatchType = "none";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fish_picker_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOkButton = (Button) view.findViewById(R.id.dialog_fish_picker_button_ok);
        mCancelButton = (Button) view.findViewById(R.id.dialog_fish_picker_button_cancel);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCr = getActivity().getContentResolver();

                ContentValues values = new ContentValues();
                values.put(TrackContentProvider.Schema.COL_TRACK_ID,mTrackId);
                values.put(TrackContentProvider.Schema.COL_CATCH_DESTINATION,mCatchDestination);
                values.put(TrackContentProvider.Schema.COL_FISH_FAMILY,mFishFamily);
                values.put(TrackContentProvider.Schema.COL_FISH_TAHITIAN,mFishTahitian);
                values.put(TrackContentProvider.Schema.COL_CATCH_N,mCatchN);
                values.put(TrackContentProvider.Schema.COL_CATCH_N_TYPE,mCatchType);

                mCr.insert(TrackContentProvider.poissonsUri(mTrackId),values);

                dataInputFishCaught prevActivity = (dataInputFishCaught) getActivity();
                prevActivity.setMyNameStr(Integer.toString(mCatchN)+" "+mCatchType);
                getDialog().dismiss();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        mDialogFishName = (TextView) view.findViewById(R.id.dialog_fish_picker_tahitian_name);
        mDialogFishName.setText(mFishTahitian);

        mPickerCatchN = (NumberPicker) view.findViewById(R.id.dialog_fish_picker_N);
        mPickerCatchN.setMinValue(0);
        mPickerCatchN.setMaxValue(100);
        mPickerCatchN.setOnValueChangedListener(this);
        mPickerCatchN.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        mPickerCatchType = (NumberPicker) view.findViewById(R.id.dialog_fish_picker_type);
        type = getResources().getStringArray(R.array.data_input_catch_sale_type);
        mPickerCatchType.setMinValue(0);
        mPickerCatchType.setMaxValue(type.length-1);
        mPickerCatchType.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return type[value];
            }
        });
        mPickerCatchType.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mPickerCatchType.setDisplayedValues(type);
        mPickerCatchType.setOnValueChangedListener(this);

        // Fetch arguments from bundle and set title
        String title = mFishFamily;
        getDialog().setTitle(title);

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        if(picker.getId() == R.id.dialog_fish_picker_N){
            mCatchN = newVal;
        }
        if(picker.getId() == R.id.dialog_fish_picker_type){
            mCatchTypeInt = newVal;
            mCatchType = type[newVal];
        }
    }
}
