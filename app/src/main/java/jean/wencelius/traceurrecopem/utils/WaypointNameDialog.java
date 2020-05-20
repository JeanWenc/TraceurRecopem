package jean.wencelius.traceurrecopem.utils;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.UUID;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.db.TrackContentProvider;
import jean.wencelius.traceurrecopem.recopemValues;

/**
 * Created by Jean Wencélius on 19/05/2020.
 */
public class WaypointNameDialog extends DialogFragment{
    private static final String ARG_TRACK_ID = "trackId";

    private long trackId;

    private EditText mInputWaypointName;

    private Button mOkButton;

    private Button mCancelButton;

    public WaypointNameDialog(){}

    public static WaypointNameDialog newInstance(long trackId) {
        WaypointNameDialog fragment = new WaypointNameDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_TRACK_ID, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trackId = getArguments().getLong(ARG_TRACK_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waypoint_name_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       mInputWaypointName = (EditText) view.findViewById(R.id.dialog_waypoint_name_input);
       mOkButton = (Button) view.findViewById(R.id.dialog_waypoint_name_button_ok);
       mCancelButton = (Button) view.findViewById(R.id.dialog_waypoint_name_button_cancel);
       mOkButton.setEnabled(false);

       mInputWaypointName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mOkButton.setEnabled(s.toString().length()!=0);
                }
                @Override
                public void afterTextChanged(Editable s) {

                }
            });


       mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String wayPointUuid = UUID.randomUUID().toString();

                //DialogInterface to get name of Waypoint

                Intent intent = new Intent(recopemValues.INTENT_TRACK_WP);
                intent.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, trackId);
                intent.putExtra(recopemValues.INTENT_KEY_UUID, wayPointUuid);
                intent.putExtra(recopemValues.INTENT_KEY_NAME, mInputWaypointName.getText().toString());
                getActivity().sendBroadcast(intent);
                getDialog().dismiss();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

    }

}
