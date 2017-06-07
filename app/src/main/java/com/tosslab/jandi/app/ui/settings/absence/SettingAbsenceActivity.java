package com.tosslab.jandi.app.ui.settings.absence;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.CalendarDialogFragment;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.settings.absence.dagger.DaggerSettingAbsenceComponent;
import com.tosslab.jandi.app.ui.settings.absence.dagger.SettingAbsenceModule;
import com.tosslab.jandi.app.ui.settings.absence.presenter.SettingAbsencePresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 2017. 5. 23..
 */

public class SettingAbsenceActivity extends BaseAppCompatActivity implements SettingAbsencePresenter.View {

    @Inject
    SettingAbsencePresenter settingAbsencePresenter;

    @Bind(R.id.vg_setting_absence_checkbox)
    SettingsBodyCheckView vgSettingAbsenceCheckBox;

    @Bind(R.id.vg_setting_absence_detail)
    ViewGroup vgSettingAbsenceDetail;

    @Bind(R.id.tv_select_absence_start_time)
    TextView tvSelectAbsenceStartTime;

    @Bind(R.id.tv_select_absence_end_time)
    TextView tvSelectAbsenceEndTime;

    @Bind(R.id.vg_setting_push_alarm_enable_checkbox)
    SettingsBodyCheckView vgSettingPushAlarmEnableCheckBox;

    @Bind(R.id.et_absence_option_message)
    EditText etAbsenceOptionMessage;

    @Bind(R.id.tv_absence_option_message_length)
    TextView tvAbsenceOptionMessageLength;

    @Bind(R.id.tv_absence_period)
    TextView tvAbsencePeriod;

    @Bind(R.id.v_scroll)
    ScrollView scrollView;

    private boolean isEnablePushAlarm = false;
    private Date startDate;
    private Date endDate;
    private Menu menu;
    private ProgressWheel progressWheel;

    public static void launchActivity(Activity activity) {
        activity.startActivity(
                new Intent(activity, SettingAbsenceActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_absence);
        ButterKnife.bind(this);
        DaggerSettingAbsenceComponent.builder()
                .settingAbsenceModule(new SettingAbsenceModule(this))
                .build()
                .inject(this);
        initViews();
    }

    private void initViews() {
        setupActionBar();
        settingAbsencePresenter.onInit();
        etAbsenceOptionMessage.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setOptionTextLength(etAbsenceOptionMessage.length());
                setConfirmButton();
            }
        });
        initProgressWheel();
        KeyboardVisibilityEvent.setEventListener(
                this, isOpen -> {
                    if (isOpen) {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
        etAbsenceOptionMessage.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                int selectionEnd = etAbsenceOptionMessage.getSelectionEnd();
                int cnt = 0;
                for (int i = 1; i <= s.length(); i++) {
                    if (s.subSequence(i - 1, i).toString().equals("\n")) {
                        if (cnt > 0) {
                            s.replace(i - 1, i, "");
                            if (selectionEnd > i - 1) {
                                selectionEnd = i - 1;
                            }
                            etAbsenceOptionMessage.setText(s);
                        }
                        cnt++;
                    }
                }
                etAbsenceOptionMessage.setSelection(selectionEnd);
            }
        });

    }

    private void setOptionTextLength(int length) {
        tvAbsenceOptionMessageLength.setText(length + "/60");
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_done, menu);
        this.menu = menu;
        menu.findItem(R.id.action_done).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (settingAbsencePresenter.hasChangeInfo(
                        vgSettingAbsenceCheckBox.isChecked(), startDate, endDate,
                        !vgSettingPushAlarmEnableCheckBox.isChecked(),
                        etAbsenceOptionMessage.getText().toString())) {
                    showCancelDialog();
                } else {
                    finish();
                }
                return true;
            case R.id.action_done:
                settingAbsencePresenter.updateAbsence(
                        vgSettingAbsenceCheckBox.isChecked(), startDate, endDate,
                        !isEnablePushAlarm, etAbsenceOptionMessage.getText().toString());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    @OnClick(R.id.vg_setting_absence_checkbox)
    public void onSettingAbsenceCheckboxClicked() {
        boolean isChecked = vgSettingAbsenceCheckBox.isChecked();
        vgSettingAbsenceCheckBox.setChecked(!isChecked);
        if (!isChecked) {
            vgSettingAbsenceDetail.setVisibility(View.VISIBLE);
        } else {
            vgSettingAbsenceDetail.setVisibility(View.INVISIBLE);
        }
        setConfirmButton();
    }

    @Override
    @OnClick(R.id.vg_setting_push_alarm_enable_checkbox)
    public void onPushAlarmEnableCheckboxClicked() {
        boolean isChecked = vgSettingPushAlarmEnableCheckBox.isChecked();
        vgSettingPushAlarmEnableCheckBox.setChecked(!isChecked);
        isEnablePushAlarm = vgSettingPushAlarmEnableCheckBox.isChecked();
        setConfirmButton();
    }

    @OnClick(R.id.tv_select_absence_start_time)
    void showStartDateChoiceView() {
        CalendarDialogFragment fragment = new CalendarDialogFragment();
        if (startDate != null) {
            fragment.setInitDate(startDate);
        }
        fragment.setTitle(getResources().getString(R.string.vacancy_startday));
        fragment.show(getSupportFragmentManager(), "calendar_dialog");
        fragment.setOnDateListener(calendarDay -> {
            Date startDate = calendarDay.getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startDate.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            setStartDate(new Date(calendar.getTimeInMillis()));
            setPeriodView();
            setConfirmButton();
        });
    }

    @Override
    public void setPeriodView() {
        long period = endDate.getTime() - startDate.getTime();
        // 하루 = 86400000ms
        long day = (period / 86400000);
        if (day >= 0) {
            day = day + 1;
        }
        String periodDays = getResources().getString(R.string.vacancy_period_count, day + "");

        if (day < 0) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(periodDays);
            String dayString = String.valueOf(day);
            int startIndex = periodDays.indexOf(dayString);
            ssb.setSpan(new ForegroundColorSpan(Color.RED),
                    startIndex, startIndex + dayString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvAbsencePeriod.setText(ssb);
        } else {
            tvAbsencePeriod.setText(periodDays);
        }
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        tvSelectAbsenceStartTime.setText(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
    }

    @OnClick(R.id.tv_select_absence_end_time)
    void showEndDateChoiceView() {
        CalendarDialogFragment fragment = new CalendarDialogFragment();
        if (endDate != null) {
            fragment.setInitDate(endDate);
        }
        fragment.setTitle(getResources().getString(R.string.vacancy_endday));
        fragment.show(getSupportFragmentManager(), "calendar_dialog");
        fragment.setOnDateListener(calendarDay -> {
            Date endDate = calendarDay.getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(endDate.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            setEndDate(new Date(calendar.getTimeInMillis()));
            setPeriodView();
            setConfirmButton();
        });
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        tvSelectAbsenceEndTime.setText(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
    }

    @Override
    public void setOptionText(String text) {
        etAbsenceOptionMessage.setText(text);
        etAbsenceOptionMessage.setSelection(text.length());
        setOptionTextLength(text.length());
    }

    @Override
    public void showEnableAbsenceInfoToast() {
        ColoredToast.show(R.string.vacancy_toast_on);
    }

    @Override
    public void showDisableAbsenceInfoToast() {
        ColoredToast.show(R.string.vacancy_toast_off);
    }

    @Override
    public void showChangeAbsenceInfoToast() {
        ColoredToast.show(R.string.vacancy_toast_update);
    }

    @Override
    public void finish() {
        super.finish();
    }

    public void setConfirmButton() {
        if (settingAbsencePresenter.hasChangeInfo(
                vgSettingAbsenceCheckBox.isChecked(), startDate, endDate,
                !vgSettingPushAlarmEnableCheckBox.isChecked(),
                etAbsenceOptionMessage.getText().toString())) {
            if (menu != null) {
                menu.findItem(R.id.action_done).setEnabled(true);
            }
        } else {
            if (menu != null) {
                menu.findItem(R.id.action_done).setEnabled(false);
            }
        }
    }

    public void showCancelDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.vacancy_cancel_title)
                .setMessage(R.string.vacancy_cancel_desc)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.jandi_cancel), null)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> finish())
                .create().show();
    }

    @Override
    public void showInvalidDatesDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.vacancy_period_error_popup)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm), null)
                .create().show();
    }

    @Override
    public void showOver3YearsDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.vacancy_period_max3y_popup)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm), null)
                .create().show();
    }

    @Override
    public void showPastDatesDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.vacancy_period_pastperiod_popup)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm), null)
                .create().show();
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
    }

    @Override
    public void showProgressBar() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressBar() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }


}