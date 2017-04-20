package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

import java.util.Calendar;

/**
 * Created by tonyjs on 16. 6. 16..
 */
public class TimePickerDialogFragment extends DialogFragment {

    public static int MODE_START_TIME = 0x01;
    public static int MODE_END_TIME = 0x02;

    public static String ARG_MODE = "arg_mode";
    public static String ARG_INCLUDE_MINUTE = "arg_include_minute";
    public static String ARG_DEFAULT_TIME = "arg_default_time";

    private int mode = MODE_END_TIME;
    private boolean isIncludeMinute = false;
    private int defaultTime = 0700;

    public static DialogFragment newInstance() {
        return new TimePickerDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            mode = getArguments().getInt(ARG_MODE);
            isIncludeMinute = getArguments().getBoolean(ARG_INCLUDE_MINUTE);
            defaultTime = getArguments().getInt(ARG_DEFAULT_TIME);
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_timepicker, null);

        final NumberPicker numberPickerHour = (NumberPicker) view.findViewById(R.id.number_picker_hour);
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);


        final TextView tvColon = (TextView) view.findViewById(R.id.tv_colon);
        final TextView tvHour = (TextView) view.findViewById(R.id.tv_hour);

        // NumberPicker TextSize 가져옴
        for (int i = 0; i < numberPickerHour.getChildCount(); i++) {
            View child = numberPickerHour.getChildAt(i);
            if (child != null && child instanceof TextView) {
                float textSize = ((TextView) child).getTextSize();
                tvColon.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tvHour.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                break;
            }
        }

        Calendar calendar = Calendar.getInstance();
        final NumberPicker numberPickerMinute = (NumberPicker) view.findViewById(R.id.number_picker_minute);
        if (isIncludeMinute) {
            String[] minuteValues = {"0", "30"};
            numberPickerMinute.setMinValue(0);
            numberPickerMinute.setMaxValue(1);
            numberPickerMinute.setDisplayedValues(minuteValues);
            tvHour.setVisibility(View.GONE);
            numberPickerHour.setValue(defaultTime / 100);
            numberPickerMinute.setValue((defaultTime % 100) / 30);
        } else {
            numberPickerMinute.setVisibility(View.GONE);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            numberPickerHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
        }

        String dialogTitle;

        if (mode == MODE_START_TIME) {
            dialogTitle = getString(R.string.push_schedule_start);
        } else {
            dialogTitle = getString(R.string.push_schedule_end);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(dialogTitle)
                .setView(view)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, numberPickerHour.getValue());
                    if (isIncludeMinute) {
                        calendar.set(Calendar.MINUTE, numberPickerMinute.getValue());
                    }

                    if (mode == MODE_END_TIME) {
                        if (getActivity() instanceof OnEndHourSelectedListener) {
                            ((OnEndHourSelectedListener) getActivity()).onEndHourSelected(calendar);
                        }
                    } else {
                        if (getActivity() instanceof OnStartHourSelectedListener) {
                            ((OnStartHourSelectedListener) getActivity()).onStartHourSelected(calendar);
                        }
                    }

                    dismiss();
                })
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public interface OnEndHourSelectedListener {
        void onEndHourSelected(Calendar calendar);
    }

    public interface OnStartHourSelectedListener {
        void onStartHourSelected(Calendar calendar);
    }

}
