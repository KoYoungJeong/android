package com.tosslab.jandi.app.ui.settings.push;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.push.model.NotificationSoundDialog;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

@EFragment(R.layout.fragment_settings_push)
public class SettingsPushFragment extends Fragment {

    @ViewById(R.id.vg_settings_push_notification)
    SettingsBodyCheckView sbcvPush;
    @ViewById(R.id.vg_settings_push_sound)
    SettingsBodyCheckView sbcvSound;
    @ViewById(R.id.vg_settings_push_sound_sub)
    LinearLayout vgSoundSub;
    @ViewById(R.id.vg_settings_push_sound_sub_topic_message)
    SettingsBodyView sbcvSoundSubTopic;
    @ViewById(R.id.vg_settings_push_sound_sub_direct_message)
    SettingsBodyView sbcvSoundSubDirectMessage;
    @ViewById(R.id.vg_settings_push_sound_sub_mentions)
    SettingsBodyView sbcvSoundSubMentions;
    @ViewById(R.id.vg_settings_push_vibration)
    SettingsBodyCheckView sbcvVibration;
    @ViewById(R.id.vg_settings_push_led)
    SettingsBodyCheckView sbcvLed;
    @ViewById(R.id.vg_settings_push_preview)
    SettingsBodyView sbvPreview;
    private SharedPreferences sharedPreferences;

    @AfterInject
    void initObject() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @AfterViews
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

    @Click(R.id.vg_settings_push_notification)
    void onNotificationClick() {
        boolean checked = !sbcvPush.isChecked();
        sbcvPush.setChecked(checked);
        setUpPushEnabled(checked);
        setPushOnValue(checked);
        setPushOnSummary(checked);

        setUpParseValue(checked);
    }

    private void setUpParseValue(boolean checked) {
        String value = checked ? ParseUpdateUtil.PARSE_ACTIVATION_ON : ParseUpdateUtil.PARSE_ACTIVATION_OFF;
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (installation.containsKey(ParseUpdateUtil.PARSE_ACTIVATION)) {
            String isPushOn = (String) installation.get(ParseUpdateUtil.PARSE_ACTIVATION);
            if (TextUtils.equals(isPushOn, value)) {
                return;
            }
        }

        installation.put(ParseUpdateUtil.PARSE_ACTIVATION, value);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                if (checked) {
                    ColoredToast.show(activity.getString(R.string.jandi_setting_push_subscription_ok));
                } else {
                    ColoredToast.show(activity.getString(R.string.jandi_setting_push_subscription_cancel));
                }
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

    @Click(R.id.vg_settings_push_sound)
    void onSoundClick() {
        boolean checked = !sbcvSound.isChecked();
        sbcvSound.setChecked(checked);
        setSoundSubVisible(checked);
        setSoundOnValue(checked);
        setSoundOnSummary(checked);
    }

    private void setSoundOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_SOUND, checked)
                .apply();
    }

    private void setSoundOnSummary(boolean checked) {
        sbcvSound.setSummary(checked ? R.string.jandi_on : R.string.jandi_off);
    }

    @Click(R.id.vg_settings_push_sound_sub_topic_message)
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

    @Click(R.id.vg_settings_push_sound_sub_direct_message)
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

    @Click(R.id.vg_settings_push_sound_sub_mentions)
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

    @Click(R.id.vg_settings_push_vibration)
    void onVibrationClick() {
        boolean checked = !sbcvVibration.isChecked();
        sbcvVibration.setChecked(checked);
        setVibrationOnValue(checked);
    }

    private void setVibrationOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_VIBRATION, checked)
                .apply();
    }

    @Click(R.id.vg_settings_push_led)
    void onLedClick() {
        boolean checked = !sbcvLed.isChecked();
        sbcvLed.setChecked(checked);
        setLedOnValue(checked);
    }

    private void setLedOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_LED, checked)
                .apply();
    }


    @Click(R.id.vg_settings_push_preview)
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
