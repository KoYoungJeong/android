package com.tosslab.jandi.app.ui.settings.push.schedule;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.TimePickerDialogFragment;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.settings.push.schedule.dagger.DaggerSettingPushScheduleComponent;
import com.tosslab.jandi.app.ui.settings.push.schedule.dagger.SettingPushScheduleModule;
import com.tosslab.jandi.app.ui.settings.push.schedule.presenter.SettingPushSchedulePresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 2017. 4. 18..
 */

public class SettingPushScheduleActivity extends BaseAppCompatActivity
        implements TimePickerDialogFragment.OnStartHourSelectedListener,
        TimePickerDialogFragment.OnEndHourSelectedListener,
        SettingPushSchedulePresenter.View {

    public static final int REQUEST_CODE = 0x01;

    @Inject
    SettingPushSchedulePresenter settingPushSchedulePresenter;

    @Bind(R.id.tv_mon_button)
    TextView tvMonButton;

    @Bind(R.id.tv_tue_button)
    TextView tvTueButton;

    @Bind(R.id.tv_wed_button)
    TextView tvWedButton;

    @Bind(R.id.tv_thu_button)
    TextView tvThuButton;

    @Bind(R.id.tv_fri_button)
    TextView tvFriButton;

    @Bind(R.id.tv_sat_button)
    TextView tvSatButton;

    @Bind(R.id.tv_sun_button)
    TextView tvSunButton;

    @Bind(R.id.tv_select_start_time)
    TextView tvSelectStartTime;

    @Bind(R.id.tv_select_end_time)
    TextView tvSelectEndTime;

    // 월요일부터 0 - 일요일 6
    private HashMap<Integer, Boolean> dayClickedInfoHashMap;
    private int selectedDayCount = 0;

    private int startTime = 700;
    private int endTime = 1900;

    private ProgressDialog progressWheel;

    public static void launchActivity(Fragment fragment) {
        fragment.startActivityForResult(
                new Intent(fragment.getContext(), SettingPushScheduleActivity.class), REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_alarm_push_schedule);
        ButterKnife.bind(this);
        DaggerSettingPushScheduleComponent.builder()
                .settingPushScheduleModule(new SettingPushScheduleModule(this))
                .build()
                .inject(this);
        initToolbar();
        dayClickedInfoHashMap = new HashMap<>();
        initViews();
    }

    private void initViews() {
        settingPushSchedulePresenter.initValues();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        new ColorDrawable(getResources().getColor(android.R.color.transparent));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            settingPushSchedulePresenter
                    .setAlarmSchedule(dayClickedInfoHashMap, startTime, endTime);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.tv_mon_button)
    void onDayMonClicked() {
        if (dayClickedInfoHashMap.get(0) == null || !dayClickedInfoHashMap.get(0)) {
            dayClickedInfoHashMap.put(0, true);
            tvMonButton.setTextColor(getResources().getColor(R.color.white));
            tvMonButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(0, false);
            tvMonButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvMonButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }
        if (selectedDayCount == 0) {
            onDayMonClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.tv_tue_button)
    void onDayTueClicked() {
        if (dayClickedInfoHashMap.get(1) == null || !dayClickedInfoHashMap.get(1)) {
            dayClickedInfoHashMap.put(1, true);
            tvTueButton.setTextColor(getResources().getColor(R.color.white));
            tvTueButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(1, false);
            tvTueButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvTueButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }

        if (selectedDayCount == 0) {
            onDayTueClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.tv_wed_button)
    void onDayWedClicked() {
        if (dayClickedInfoHashMap.get(2) == null || !dayClickedInfoHashMap.get(2)) {
            dayClickedInfoHashMap.put(2, true);
            tvWedButton.setTextColor(getResources().getColor(R.color.white));
            tvWedButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(2, false);
            tvWedButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvWedButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }

        if (selectedDayCount == 0) {
            onDayWedClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.tv_thu_button)
    void onDayThuClicked() {
        if (dayClickedInfoHashMap.get(3) == null || !dayClickedInfoHashMap.get(3)) {
            dayClickedInfoHashMap.put(3, true);
            tvThuButton.setTextColor(getResources().getColor(R.color.white));
            tvThuButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(3, false);
            tvThuButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvThuButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }

        if (selectedDayCount == 0) {
            onDayThuClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.tv_fri_button)
    void onDayFriClicked() {
        if (dayClickedInfoHashMap.get(4) == null || !dayClickedInfoHashMap.get(4)) {
            dayClickedInfoHashMap.put(4, true);
            tvFriButton.setTextColor(getResources().getColor(R.color.white));
            tvFriButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(4, false);
            tvFriButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvFriButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }

        if (selectedDayCount == 0) {
            onDayFriClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.tv_sat_button)
    void onDaySatClicked() {
        if (dayClickedInfoHashMap.get(5) == null || !dayClickedInfoHashMap.get(5)) {
            dayClickedInfoHashMap.put(5, true);
            tvSatButton.setTextColor(getResources().getColor(R.color.white));
            tvSatButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(5, false);
            tvSatButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvSatButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }

        if (selectedDayCount == 0) {
            onDaySatClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.tv_sun_button)
    void onDaySunClicked() {
        if (dayClickedInfoHashMap.get(6) == null || !dayClickedInfoHashMap.get(6)) {
            dayClickedInfoHashMap.put(6, true);
            tvSunButton.setTextColor(getResources().getColor(R.color.white));
            tvSunButton.setBackground(getResources().getDrawable(R.drawable.notification_day_on));
            selectedDayCount++;
        } else {
            dayClickedInfoHashMap.put(6, false);
            tvSunButton.setTextColor(getResources().getColor(R.color.rgb_cccccc));
            tvSunButton.setBackground(getResources().getDrawable(R.drawable.notification_day_off));
            selectedDayCount--;
        }

        if (selectedDayCount == 0) {
            onDaySunClicked();
            showLeastOnedaySelectToast();
        }
    }

    @OnClick(R.id.vg_select_start_time)
    void onStartTimeClicked() {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TimePickerDialogFragment.ARG_MODE, TimePickerDialogFragment.MODE_START_TIME);
        bundle.putBoolean(TimePickerDialogFragment.ARG_INCLUDE_MINUTE, true);
        bundle.putInt(TimePickerDialogFragment.ARG_DEFAULT_TIME, startTime);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "time_picker_dialog");
    }

    @OnClick(R.id.vg_select_end_time)
    void onEndTimeClicked() {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TimePickerDialogFragment.ARG_MODE, TimePickerDialogFragment.MODE_END_TIME);
        bundle.putBoolean(TimePickerDialogFragment.ARG_INCLUDE_MINUTE, true);
        bundle.putInt(TimePickerDialogFragment.ARG_DEFAULT_TIME, endTime);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "time_picker_dialog");
    }

    @Override
    public void onEndHourSelected(Calendar calendar) {
        int endTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endTimeMinute = calendar.get(Calendar.MINUTE);
        LogUtil.e("endTimeHour", endTimeHour + "");
        LogUtil.e("endTimeMinute", endTimeMinute + "");
        int tempEndTime = endTimeHour * 100 + endTimeMinute * 30;
        if (tempEndTime != startTime) {
            endTime = tempEndTime;
        } else {
            ColoredToast.showError(getString(R.string.push_schedule_nosametime));
        }
        setEndTime(endTime);
    }

    @Override
    public void onStartHourSelected(Calendar calendar) {
        int startTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startTimeMinute = calendar.get(Calendar.MINUTE);
        LogUtil.e("startTimeHour", startTimeHour + "");
        LogUtil.e("startTimeMinute", startTimeMinute + "");
        int tempStartTime = startTimeHour * 100 + startTimeMinute * 30;
        if (tempStartTime != endTime) {
            startTime = tempStartTime;
        } else {
            ColoredToast.showError(getString(R.string.push_schedule_nosametime));
        }
        setStartTime(startTime);
    }

    @Override
    public void setStartTime(int startTime) {
        this.startTime = startTime;
        tvSelectStartTime.setText(getIntTimeToString(startTime));
    }

    @Override
    public void setEndTime(int endTime) {
        this.endTime = endTime;
        tvSelectEndTime.setText(getIntTimeToString(endTime));
    }

    @Override
    public void setDays(List<Integer> days) {
        for (int day : days) {
            switch (day) {
                case 0:
                    onDayMonClicked();
                    break;
                case 1:
                    onDayTueClicked();
                    break;
                case 2:
                    onDayWedClicked();
                    break;
                case 3:
                    onDayThuClicked();
                    break;
                case 4:
                    onDayFriClicked();
                    break;
                case 5:
                    onDaySatClicked();
                    break;
                case 6:
                    onDaySunClicked();
                    break;
            }
        }
    }

    private String getIntTimeToString(int time) {
        int hour = time / 100;
        int minute = time % 100;
        StringBuilder stringBuilder = new StringBuilder();
        if (hour >= 12) {
            stringBuilder.append(getString(R.string.jandi_date_evening));
            stringBuilder.append(" ");
            hour = hour - 12;
            if (hour == 0) {
                hour = 12;
            }
        } else {
            stringBuilder.append(getString(R.string.jandi_date_morning));
            stringBuilder.append(" ");
        }

        if (hour < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(hour);
        stringBuilder.append(":");
        if (minute < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(minute);
        return stringBuilder.toString();
    }

    @Override
    public void showLeastOnedaySelectToast() {
        ColoredToast.showError(R.string.push_schedule_minoneday);
    }

    @Override
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void finishActivity(boolean result) {
        setResult(RESULT_OK);
        finish();
    }

}