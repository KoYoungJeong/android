package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.DayFormatter;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.Calendar;

/**
 * Created by tonyjs on 16. 6. 16..
 */
public class CalendarDialogFragment extends DialogFragment {

    public static DialogFragment newInstance() {
        return new CalendarDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_calendar, null);

        final MaterialCalendarView calendarView = (MaterialCalendarView) view.findViewById(R.id.calendar_view);
        initCalendarView(calendarView);

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.jandi_verify_fingerprint)
                .setView(view)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    if (getActivity() instanceof OnDateSelectedListener) {
                        ((OnDateSelectedListener) getActivity())
                                .onDateSelected(calendarView.getSelectedDate());
                    }
                    dismiss();
                })
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }

    private void initCalendarView(MaterialCalendarView calendarView) {
        calendarView.setDayFormatter(DayFormatter.DEFAULT);
        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        int selectionColor = calendarView.getResources()
                .getColor(R.color.jandi_text_messages_integration_image);
        calendarView.setSelectionColor(selectionColor);
        calendarView.setDateTextAppearance(R.style.DayTextAppearance);

        MaterialCalendarView.StateBuilder stateBuilder = calendarView.state().edit();
        Calendar calendar = Calendar.getInstance();
        stateBuilder.setMinimumDate(calendar);
        calendarView.setCurrentDate(calendar);
        calendarView.setDateSelected(calendar, true);
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        stateBuilder.setMaximumDate(calendar);
        stateBuilder.commit();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(CalendarDay calendarDay);
    }
}
