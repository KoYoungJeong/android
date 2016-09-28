package com.tosslab.jandi.app.ui.settings.push;

import android.content.Context;
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

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSubscribeToken;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.push.dagger.DaggerSettingsPushComponent;
import com.tosslab.jandi.app.ui.settings.push.model.NotificationSoundDialog;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;

import org.androidannotations.annotations.EFragment;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_settings_push)
public class SettingsPushFragment extends Fragment {

    @Bind(R.id.vg_settings_push_notification)
    SettingsBodyCheckView sbcvPush;
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
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    Lazy<DeviceApi> deviceApi;


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
        sbcvSound.setEnabled(pushOn);
        sbcvLed.setEnabled(pushOn);
        sbcvVibration.setEnabled(pushOn);
        sbcvSoundSubTopic.setEnabled(pushOn);
        sbcvSoundSubDirectMessage.setEnabled(pushOn);
        sbcvSoundSubMentions.setEnabled(pushOn);
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

    private void sendAnalyticsEvent(AnalyticsValue.Action action, boolean on) {
        AnalyticsValue.Label label = on ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off;
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, action, label);
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
        sbcvSound.setSummary(checked ? R.string.jandi_auto_join_on : R.string.jandi_auto_join_off);
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
                    }
                    dialog.dismiss();
                });
        builder.create().show();

    }
}
