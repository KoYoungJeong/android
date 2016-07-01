package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
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

    public static DialogFragment newInstance() {
        return new TimePickerDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_timepicker, null);

        final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(23);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        numberPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));

        final TextView tvColon = (TextView) view.findViewById(R.id.tv_colon);
        final TextView tvHour = (TextView) view.findViewById(R.id.tv_hour);

        // NumberPicker TextSize 가져옴
        for (int i = 0; i < numberPicker.getChildCount(); i++) {
            View child = numberPicker.getChildAt(i);
            if (child != null && child instanceof TextView) {
                float textSize = ((TextView) child).getTextSize();
                tvColon.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tvHour.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                break;
            }
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.jandi_duedate)
                .setView(view)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    if (getActivity() instanceof OnHourSelectedListener) {
                        ((OnHourSelectedListener) getActivity()).onHourSelected(numberPicker.getValue());
                    }
                    dismiss();
                })
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }

    public interface OnHourSelectedListener {
        void onHourSelected(int hour);
    }
}
