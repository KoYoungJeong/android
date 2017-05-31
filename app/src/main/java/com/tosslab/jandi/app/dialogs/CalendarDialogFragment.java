package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.DayFormatter;
import com.tosslab.jandi.app.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by tonyjs on 16. 6. 16..
 */
public class CalendarDialogFragment extends DialogFragment {

    private OnDateSelectedListener onDateSelectedListener;
    private MaterialCalendarView calendarView;
    private Date initialDay = null;
    private String title;

    public static DialogFragment newInstance() {
        return new CalendarDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_calendar, null);

        calendarView = (MaterialCalendarView) view.findViewById(R.id.calendar_view);
        initCalendarView();

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(title != null ? title : getString(R.string.jandi_duedate))
                .setView(view)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    if (onDateSelectedListener != null) {
                        onDateSelectedListener.onDateSelected(calendarView.getSelectedDate());
                    } else if (getActivity() instanceof OnDateSelectedListener) {
                        ((OnDateSelectedListener) getActivity())
                                .onDateSelected(calendarView.getSelectedDate());
                    }
                    dismiss();
                })
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }

    private void initCalendarView() {
        calendarView.setDayFormatter(DayFormatter.DEFAULT);
        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        int selectionColor = calendarView.getResources()
                .getColor(R.color.jandi_text_messages_integration_image);
        calendarView.setSelectionColor(selectionColor);
        calendarView.setDateTextAppearance(R.style.DayTextAppearance);

        MaterialCalendarView.StateBuilder stateBuilder = calendarView.state().edit();
        Calendar calendar = Calendar.getInstance();
        if (initialDay != null) {
            calendar.setTimeInMillis(initialDay.getTime());
        }
        calendarView.setDateSelected(calendar, true);
        calendarView.setCurrentDate(Calendar.getInstance());
        if (initialDay == null) {
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            stateBuilder.setMaximumDate(calendar);
            stateBuilder.setMinimumDate(Calendar.getInstance());
        }
        stateBuilder.commit();
    }

    public void setInitDate(Date date) {
        this.initialDay = date;
    }

    public void setOnDateListener(OnDateSelectedListener onDateSelectedListener) {
        this.onDateSelectedListener = onDateSelectedListener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(CalendarDay calendarDay);
    }
}
