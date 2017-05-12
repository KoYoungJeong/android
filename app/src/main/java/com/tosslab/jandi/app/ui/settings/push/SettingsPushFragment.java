package com.tosslab.jandi.app.ui.settings.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;
import com.tosslab.jandi.app.network.models.ResDeviceSubscribe;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.push.dagger.DaggerSettingsPushComponent;
import com.tosslab.jandi.app.ui.settings.push.model.NotificationSoundDialog;
import com.tosslab.jandi.app.ui.settings.push.schedule.SettingPushScheduleActivity;
import com.tosslab.jandi.app.ui.settings.push.schedule.model.SettingPushScheduleModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class SettingsPushFragment extends Fragment {

    @Bind(R.id.vg_settings_push_notification)
    SettingsBodyCheckView sbcvPush;
    @Bind(R.id.vg_settings_push_notification_schedule)
    SettingsBodyCheckView sbcvPushSchedule;
    @Bind(R.id.vg_settings_push_sound)
    SettingsBodyCheckView sbcvSound;
    @Bind(R.id.vg_settings_push_sound_sub)
    LinearLayout vgSoundSub;
    @Bind(R.id.vg_settings_push_sound_sub_topic_message)
    SettingsBodyView sbcvSoundSubTopic;
    @Bind(R.id.vg_settings_push_sound_sub_direct_message)
    SettingsBodyView sbcvSoundSubDirectMessage;
    @Bind(R.id.vg_settings_push_sound_sub_mentions)
    SettingsBodyView sbcvSoundSubMentions;
    @Bind(R.id.vg_settings_push_vibration)
    SettingsBodyCheckView sbcvVibration;
    @Bind(R.id.vg_settings_push_led)
    SettingsBodyCheckView sbcvLed;
    @Bind(R.id.vg_settings_push_preview)
    SettingsBodyView sbvPreview;

    @Bind(R.id.vg_notification_schedule_detail)
    ViewGroup vgNotificationScheduleDetail;

    @Bind(R.id.tv_push_schedule_weekdays)
    TextView tvPushScheduleWeekdays;
    @Bind(R.id.tv_push_schedule_start_time)
    TextView tvPushScheduleStartTime;
    @Bind(R.id.tv_push_schedule_end_time)
    TextView tvPushScheduleEndTime;

    @Bind(R.id.tv_push_schedule_weekdays_title)
    TextView tvPushScheduleWeekdaysTitle;
    @Bind(R.id.tv_push_schedule_start_time_title)
    TextView tvPushScheduleStartTimeTitle;
    @Bind(R.id.tv_push_schedule_end_time_title)
    TextView tvPushScheduleEndTimeTitle;

    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    Lazy<DeviceApi> deviceApi;
    @Inject
    SettingPushScheduleModel settingPushScheduleModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_push, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DaggerSettingsPushComponent.builder()
                .build()
                .inject(this);
        initViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingPushScheduleActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setUpPushSchedule(true);
            }
        }
    }

    void initViews() {
        boolean pushOn = sharedPreferences.getBoolean(Settings.SETTING_PUSH_AUTO_ALARM, true);
        boolean soundOn = sharedPreferences.getBoolean(Settings.SETTING_PUSH_ALARM_SOUND, true);
        boolean ledOn = sharedPreferences.getBoolean(Settings.SETTING_PUSH_ALARM_LED, true);
        boolean vibrationOn = sharedPreferences.getBoolean(Settings.SETTING_PUSH_ALARM_VIBRATION, true);

        sbcvPush.setChecked(pushOn);
        setPushOnSummary(pushOn);
        sbcvSound.setChecked(soundOn);
        setSoundOnSummary(soundOn);
        setSoundSubVisible(soundOn);
        setSoundSubStatus();
        sbcvLed.setChecked(ledOn);
        sbcvVibration.setChecked(vibrationOn);

        setUpPushEnabled(pushOn);
        setUpPreview();
        setUpPushSchedule(pushOn);
    }

    private void setUpPushSchedule(boolean pushOn) {
        if (!pushOn) {
            sbcvPushSchedule.setEnabled(false);
        } else {
            sbcvPushSchedule.setEnabled(true);
        }

        boolean hasScheduleCache = Settings.hasAlarmScheduleCache();
        boolean isScheduleOn = Settings.getPreferencePushAlarmSchedule();

        if (hasScheduleCache) {
            if (isScheduleOn) {
                setAlarmScheduleDetail(Settings.getPreferencePushAlarmScheduleDays(),
                        Settings.getPreferencePushAlarmScheduleStartTime(),
                        Settings.getPreferencePushAlarmScheduleEndTime(),
                        Settings.getPreferencePushAlarmScheduleTimeZone());
            } else {
                vgNotificationScheduleDetail.setVisibility(View.GONE);
            }
        } else {
            Observable.defer(() -> {
                ResDeviceSubscribe resDeviceSubscribe = settingPushScheduleModel.getDeviceInfo();
                return Observable.just(resDeviceSubscribe);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deviceInfo -> {
                        setAlarmScheduleDetail(deviceInfo.getDays(),
                                deviceInfo.getStartTime(),
                                deviceInfo.getEndTime(),
                                deviceInfo.getTimezone());
                    });
        }
    }

    private void setAlarmScheduleDetail(List<Integer> dayList, int startTime, int endTime, int timeZone) {
        if (dayList != null && dayList.size() > 0) {
            Settings.setHasAlarmSchedule(true);
            Settings.setPreferencePushAlarmSchedule(true);
            Settings.setPreferencePushAlarmScheduleStartTime(startTime);
            Settings.setPreferencePushAlarmScheduleEndTime(endTime);
            Settings.setPreferencePushAlarmScheduleDays(dayList);
            Settings.setPreferencePushAlarmScheduleTimeZone(timeZone);
            sbcvPushSchedule.setChecked(true);
            vgNotificationScheduleDetail.setVisibility(View.VISIBLE);

            int timeZoneDistance = 0;

            if (timeZone != -100) {
                timeZoneDistance = getTimeZoneInt() - timeZone;
            }

            // 표준시로 인해 날짜가 바뀔 수 있음을 염두
            boolean dayPlus = false;
            boolean dayMinus = false;

            if (startTime != -1) {
                int startTimeHour = startTime / 100;
                startTimeHour = startTimeHour + timeZoneDistance;
                if (startTimeHour < 0) {
                    startTimeHour = 24 - startTimeHour;
                    dayMinus = true;
                } else if (startTimeHour > 23) {
                    startTimeHour = startTimeHour - 24;
                    dayPlus = true;
                }
                tvPushScheduleStartTime.setText(getIntTimeToString(startTimeHour * 100 + startTime % 100));
            }

            if (endTime != -1) {
                int endTimeHour = endTime / 100;
                endTimeHour = endTimeHour - timeZoneDistance;
                if (endTimeHour < 0) {
                    endTimeHour = 24 - endTimeHour;
                } else if (endTimeHour > 23) {
                    endTimeHour = endTimeHour - 24;
                }
                tvPushScheduleEndTime.setText(getIntTimeToString(endTimeHour * 100 + endTime % 100));
            }

            if (dayMinus) {
                for (int i = 0; i < dayList.size(); i++) {
                    int replaceDay = dayList.get(i) - 1;
                    if (replaceDay < 0) {
                        replaceDay = 6;
                    }
                    dayList.set(i, replaceDay);
                }
            } else if (dayPlus) {
                for (int i = 0; i < dayList.size(); i++) {
                    int replaceDay = dayList.get(i) + 1;
                    if (replaceDay > 6) {
                        replaceDay = 0;
                    }
                    dayList.set(i, replaceDay);
                }
            }

            int bitSum = 0; // 일 2 월 4 화 8 수 16 목 32 금 64 토 128
            int weekBit;
            StringBuilder selectedDaysSB = new StringBuilder();

            if (dayList.get(0) == 0) {
                dayList.remove(0);
                dayList.add(0);
            }

            for (int day : dayList) {
                weekBit = 1;
                for (int i = 0; i <= day; i++) {
                    weekBit = weekBit * 2;
                }
                bitSum += weekBit;
                switch (day) {
                    case 1:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_mon) + ", ");
                        break;
                    case 2:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_tue) + ", ");
                        break;
                    case 3:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_wed) + ", ");
                        break;
                    case 4:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_thu) + ", ");
                        break;
                    case 5:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_fri) + ", ");
                        break;
                    case 6:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_sat) + ", ");
                        break;
                    case 0:
                        selectedDaysSB.append(getResources().getString(R.string.day_short_sun) + ", ");
                        break;

                }
            }

            // 주중
            if (bitSum == 124) {
                tvPushScheduleWeekdays.setText(getString(R.string.push_schedule_weekdays));
            }
            // 주말
            else if (bitSum == 130) {
                tvPushScheduleWeekdays.setText(getString(R.string.push_schedule_weekend));
            }
            // 매일
            else if (bitSum == 254) {
                tvPushScheduleWeekdays.setText(getString(R.string.day_everyday));
            }
            // 선택된 날짜
            else {
                tvPushScheduleWeekdays.setText(selectedDaysSB.delete(
                        selectedDaysSB.length() - 2, selectedDaysSB.length() - 1));
            }

        } else {
            sbcvPushSchedule.setChecked(false);
            vgNotificationScheduleDetail.setVisibility(View.GONE);
        }

    }

    private void setSoundSubStatus() {
        int topicSoundIdx = sharedPreferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_TOPIC, 0);
        int directMessageSoundIdx = sharedPreferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_DM, 0);
        int mentionSoundIdx = sharedPreferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_MENTION, 0);

        Resources resources = JandiApplication.getContext().getResources();
        String[] soundSummary = resources.getStringArray(R.array.jandi_notification_array_text);

        String topicSound = soundSummary[topicSoundIdx];
        String directMessageSound = soundSummary[directMessageSoundIdx];
        String mentionSound = soundSummary[mentionSoundIdx];

        sbcvSoundSubTopic.setSummary(topicSound);
        sbcvSoundSubDirectMessage.setSummary(directMessageSound);
        sbcvSoundSubMentions.setSummary(mentionSound);
    }

    private void setSoundSubVisible(boolean soundOn) {
        int visible;
        if (soundOn) {
            visible = View.VISIBLE;
        } else {
            visible = View.GONE;
        }
        vgSoundSub.setVisibility(visible);
    }

    private void setUpPreview() {
        String value = sharedPreferences.getString(Settings.SETTING_PUSH_PREVIEW, "0");
        sbvPreview.setSummary(SettingsModel.getPushPreviewSummary(value));
    }

    private void setUpPushEnabled(boolean pushOn) {
        sbcvPushSchedule.setEnabled(pushOn);
        sbcvSound.setEnabled(pushOn);
        sbcvLed.setEnabled(pushOn);
        sbcvVibration.setEnabled(pushOn);
        sbcvSoundSubTopic.setEnabled(pushOn);
        sbcvSoundSubDirectMessage.setEnabled(pushOn);
        sbcvSoundSubMentions.setEnabled(pushOn);
        if (vgNotificationScheduleDetail.getVisibility() == View.VISIBLE) {
            vgNotificationScheduleDetail.setEnabled(pushOn);
            tvPushScheduleWeekdaysTitle.setEnabled(pushOn);
            tvPushScheduleWeekdays.setEnabled(pushOn);
            tvPushScheduleStartTimeTitle.setEnabled(pushOn);
            tvPushScheduleStartTime.setEnabled(pushOn);
            tvPushScheduleEndTimeTitle.setEnabled(pushOn);
            tvPushScheduleEndTime.setEnabled(pushOn);
        }
    }

    @OnClick(R.id.vg_settings_push_notification)
    void onNotificationClick() {
        boolean checked = !sbcvPush.isChecked();
        sbcvPush.setChecked(checked);
        setUpPushEnabled(checked);
        setPushOnValue(checked);
        setPushOnSummary(checked);
        setUpParseValue(checked);
        sendAnalyticsEvent(AnalyticsValue.Action.Notifications, checked);
    }

    @OnClick(R.id.vg_settings_push_notification_schedule)
    void onNotificationScheduleClick() {

        if (!Settings.hasAlarmScheduleCache()) {
            if (!sbcvPushSchedule.isChecked()) {
                SettingPushScheduleActivity.launchActivity(this);
                return;
            }
        }

        boolean checked = !sbcvPushSchedule.isChecked();

        if (checked) {
            Observable.defer(() -> {
                List<Integer> dayList = Settings.getPreferencePushAlarmScheduleDays();
                int alarmScheduleStartTime = Settings.getPreferencePushAlarmScheduleStartTime();
                int alarmScheduleEndTime = Settings.getPreferencePushAlarmScheduleEndTime();
                boolean success = settingPushScheduleModel
                        .setAlarmSchedule(dayList, alarmScheduleStartTime, alarmScheduleEndTime, getTimeZoneInt());
                if (success) {
                    Settings.setPreferencePushAlarmScheduleDays(dayList);
                    Settings.setPreferencePushAlarmScheduleStartTime(alarmScheduleStartTime);
                    Settings.setPreferencePushAlarmScheduleEndTime(alarmScheduleEndTime);
                }
                return Observable.just(success);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        if (success) {
                            Settings.setPreferencePushAlarmSchedule(true);
                            sbcvPushSchedule.setChecked(true);
                            setUpPushSchedule(true);
                        } else {
                            // 오류 예외 처리
                        }
                    });
        } else {
            Settings.setPreferencePushAlarmSchedule(false);
            Observable.defer(() -> {
                List<Integer> dayList = new ArrayList<>();
                boolean success = settingPushScheduleModel.setAlarmSchedule(
                        dayList, 700, 1900, getTimeZoneInt());
                return Observable.just(success);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        if (success) {
                            Settings.setPreferencePushAlarmSchedule(false);
                            sbcvPushSchedule.setChecked(false);
                            vgNotificationScheduleDetail.setVisibility(View.GONE);
                        } else {
                            // 오류 예외 처리
                        }
                    });
        }
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action, boolean on) {
        AnalyticsValue.Label label = on ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting, action, label);
    }

    private void setUpParseValue(boolean checked) {
        Observable.just(1)
                .map(value -> TokenUtil.getTokenObject())
                .observeOn(Schedulers.io())
                .doOnNext(accessToken -> {
                    ReqSubscribeToken subscibeToken = new ReqSubscribeToken(checked);
                    try {
                        deviceApi.get().updateSubscribe(accessToken.getDeviceId(), subscibeToken);
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accessToken -> {
                    Context context = JandiApplication.getContext();
                    if (checked) {
                        ColoredToast.show(context.getString(R.string.jandi_setting_push_subscription_ok));
                    } else {
                        ColoredToast.show(context.getString(R.string.jandi_setting_push_subscription_cancel));
                    }
                });
    }

    private void setPushOnSummary(boolean checked) {
        sbcvPush.setSummary(checked ?
                R.string.jandi_setting_push_subscription_ok :
                R.string.jandi_setting_push_subscription_cancel);
    }

    private void setPushOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_AUTO_ALARM, checked)
                .apply();
    }


    @OnClick(R.id.vg_settings_push_sound)
    void onSoundClick() {
        boolean checked = !sbcvSound.isChecked();
        sbcvSound.setChecked(checked);
        setSoundSubVisible(checked);
        setSoundOnValue(checked);
        setSoundOnSummary(checked);

        sendAnalyticsEvent(AnalyticsValue.Action.Sounds, checked);
    }

    private void setSoundOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_SOUND, checked)
                .apply();
    }

    private void setSoundOnSummary(boolean checked) {
        sbcvSound.setSummary(checked ? R.string.jandi_on : R.string.jandi_off);
    }

    @OnClick(R.id.vg_settings_push_sound_sub_topic_message)
    void onTopicSoundClick() {
        int topicSoundIdx = sharedPreferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_TOPIC, 0);
        NotificationSoundDialog.showNotificationSound(getActivity(), topicSoundIdx,
                selectPosition -> {
                    sharedPreferences.edit()
                            .putInt(Settings.SETTING_PUSH_ALARM_SOUND_TOPIC, selectPosition)
                            .commit();
                    setSoundSubStatus();
                });
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting, AnalyticsValue.Action.Sounds_Topic);
    }

    @OnClick(R.id.vg_settings_push_sound_sub_direct_message)
    void onDirectMessageSoundClick() {
        int topicSoundIdx = sharedPreferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_DM, 0);
        NotificationSoundDialog.showNotificationSound(getActivity(), topicSoundIdx,
                selectPosition -> {
                    sharedPreferences.edit()
                            .putInt(Settings.SETTING_PUSH_ALARM_SOUND_DM, selectPosition)
                            .commit();
                    setSoundSubStatus();
                });
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting, AnalyticsValue.Action.Sounds_DM);
    }

    @OnClick(R.id.vg_settings_push_sound_sub_mentions)
    void onMentionSoundClick() {
        int topicSoundIdx = sharedPreferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_MENTION, 0);
        NotificationSoundDialog.showNotificationSound(getActivity(), topicSoundIdx,
                selectPosition -> {
                    sharedPreferences.edit()
                            .putInt(Settings.SETTING_PUSH_ALARM_SOUND_MENTION, selectPosition)
                            .commit();
                    setSoundSubStatus();
                });
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting, AnalyticsValue.Action.Sounds_Mention);
    }

    @OnClick(R.id.vg_settings_push_vibration)
    void onVibrationClick() {
        boolean checked = !sbcvVibration.isChecked();
        sbcvVibration.setChecked(checked);
        setVibrationOnValue(checked);

        sendAnalyticsEvent(AnalyticsValue.Action.Vibrate, checked);
    }

    private void setVibrationOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_VIBRATION, checked)
                .apply();
    }

    @OnClick(R.id.vg_settings_push_led)
    void onLedClick() {
        boolean checked = !sbcvLed.isChecked();
        sbcvLed.setChecked(checked);
        setLedOnValue(checked);

        sendAnalyticsEvent(AnalyticsValue.Action.PhoneLed, checked);
    }

    private void setLedOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_LED, checked)
                .apply();
    }

    @OnClick(R.id.vg_settings_push_preview)
    void onPreviewClick() {
        String value = sharedPreferences.getString(Settings.SETTING_PUSH_PREVIEW, "0");
        String[] values = getResources().getStringArray(R.array.jandi_pref_push_preview_values);
        int preselect = Math.max(0, Arrays.asList(values).indexOf(value));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setTitle(R.string.jandi_preview_message_contents)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setSingleChoiceItems(R.array.jandi_pref_push_preview, preselect, (dialog, which) -> {
                    if (which >= 0 && values != null) {
                        String selectedValue = values[which];
                        sharedPreferences.edit()
                                .putString(Settings.SETTING_PUSH_PREVIEW, selectedValue)
                                .commit();
                        setUpPreview();

                        switch (which) {
                            case 0:
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting,
                                        AnalyticsValue.Action.PreviewMsgContents, AnalyticsValue.Label.All);
                                break;
                            case 1:
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting,
                                        AnalyticsValue.Action.PreviewMsgContents, AnalyticsValue.Label.Public);
                                break;
                            case 2:
                                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.NotificationSetting,
                                        AnalyticsValue.Action.PreviewMsgContents, AnalyticsValue.Label.None);
                                break;
                        }

                    }
                    dialog.dismiss();
                });
        builder.create().show();
    }

    @OnClick(R.id.vg_notification_schedule_detail)
    void onClickNotificationScheduleDetail() {
        SettingPushScheduleActivity.launchActivity(this);
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

    private int getTimeZoneInt() {
        TimeZone timeZone = TimeZone.getDefault();
        String timeZoneString = timeZone.getDisplayName(false, TimeZone.SHORT).replace("GMT", "");

        boolean isPlus;

        if (timeZoneString.contains("+")) {
            isPlus = true;
        } else {
            isPlus = false;
        }

        int timeZoneInt = Integer.valueOf(timeZoneString.substring(1, 3));

        if (!isPlus) {
            timeZoneInt = timeZoneInt * -1;
        }
        return timeZoneInt;
    }

}
