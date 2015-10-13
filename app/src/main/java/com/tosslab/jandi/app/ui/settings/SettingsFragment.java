package com.tosslab.jandi.app.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.settings.privacy.SettingPrivacyActivity_;
import com.tosslab.jandi.app.ui.settings.viewmodel.SettingFragmentViewModel;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 18..
 */
@EFragment
public class SettingsFragment extends PreferenceFragment {

    @Bean
    SettingFragmentViewModel settingFragmentViewModel;

    @AfterViews
    void init() {
        settingFragmentViewModel.initProgress(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.SETTING)
                        .build());

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.Setting);

        addPreferencesFromResource(R.xml.pref_setting);

        boolean isPush = ((CheckBoxPreference) getPreferenceManager().findPreference("setting_push_auto_alarm")).isChecked();
        setPushSubState(isPush);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (TextUtils.equals(preference.getKey(), "setting_push_auto_alarm")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;


            boolean isEnabled;

            if (pref.isChecked()) {
                onPushNotification();
                isEnabled = true;
            } else {
                offPushNotification();
                isEnabled = false;
            }

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, isEnabled ? AnalyticsValue.Action.TurnOnNotifications : AnalyticsValue.Action.TurnOffNotifications);

            setPushSubState(isEnabled);
        } else if (TextUtils.equals(preference.getKey(), "setting_tos")) {

            TermActivity_
                    .intent(getActivity())
                    .termMode(TermActivity.Mode.Agreement.name())
                    .start();

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.TermsOfService);
        } else if (TextUtils.equals(preference.getKey(), "setting_pp")) {
            TermActivity_
                    .intent(getActivity())
                    .termMode(TermActivity.Mode.Privacy.name())
                    .start();
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.PrivacyPolicy);
        } else if(TextUtils.equals(preference.getKey(), "setting_set_passcode")) {

            SettingPrivacyActivity_.intent(getActivity())
                    .start();

        } else if (preference.getKey().equals("setting_logout")) {

            if (NetworkCheckUtil.isConnected()) {
                settingFragmentViewModel.showSignoutDialog(getActivity());
            } else {
                settingFragmentViewModel.showCheckNetworkDialog(getActivity());
            }
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.SignOut);

        } else if (preference.getKey().equals("setting_push_alarm_sound")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, pref.isChecked() ? AnalyticsValue.Action.SoundsOn : AnalyticsValue.Action.SoundsOff);
        } else if (preference.getKey().equals("setting_push_alarm_vibration")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, pref.isChecked() ? AnalyticsValue.Action.VibrateOn : AnalyticsValue.Action.VibrateOff);
        } else if (preference.getKey().equals("setting_push_alarm_led")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, pref.isChecked() ? AnalyticsValue.Action.PhoneLedOn : AnalyticsValue.Action.PhoneLedOff);
        }
        return false;
    }

    public void onEvent(SignOutEvent event) {

        if (NetworkCheckUtil.isConnected()) {
            trackSignOut();
            startSignOut();
        } else {
            settingFragmentViewModel.showCheckNetworkDialog(getActivity());
        }
    }

    @Background
    void startSignOut() {
        settingFragmentViewModel.showProgressDialog();
        try {

            SignOutUtil.removeSignData();

            Activity activity = getActivity();

            ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
            MixpanelAccountAnalyticsClient
                    .getInstance(activity, accountInfo.getId())
                    .trackAccountSigningOut()
                    .flush()
                    .clear();

            EntityManager entityManager = EntityManager.getInstance();

            MixpanelMemberAnalyticsClient
                    .getInstance(activity, entityManager.getDistictId())
                    .trackSignOut()
                    .flush()
                    .clear();

            JandiSocketService.stopService(activity);

            BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
            badgeCountRepository.deleteAll();
            BadgeUtils.clearBadge(activity);

            ColoredToast.show(activity, getString(R.string.jandi_message_logout));

        } catch (Exception e) {
        } finally {
            settingFragmentViewModel.dismissProgressDialog();
        }

        settingFragmentViewModel.returnToLoginActivity(getActivity());
    }

    private void trackSignOut() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SignOut)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .build())
                .flush();

    }

    private void setPushSubState(boolean isEnabled) {
        Preference soundPref = getPreferenceManager().findPreference("setting_push_alarm_sound");
        Preference ledPref = getPreferenceManager().findPreference("setting_push_alarm_led");
        Preference vibratePref = getPreferenceManager().findPreference("setting_push_alarm_vibration");

        soundPref.setEnabled(isEnabled);
        ledPref.setEnabled(isEnabled);
        vibratePref.setEnabled(isEnabled);
    }

    void onPushNotification() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (installation.containsKey(ParseUpdateUtil.PARSE_ACTIVATION)) {
            String isPushOn = (String) installation.get(ParseUpdateUtil.PARSE_ACTIVATION);
            if (TextUtils.equals(isPushOn, ParseUpdateUtil.PARSE_ACTIVATION_ON)) {
                return;
            }
        }

        installation.put(ParseUpdateUtil.PARSE_ACTIVATION, ParseUpdateUtil.PARSE_ACTIVATION_ON);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                ColoredToast.show(activity,
                        activity.getString(R.string.jandi_setting_push_subscription_ok));
            }
        });
    }

    void offPushNotification() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (installation.containsKey(ParseUpdateUtil.PARSE_ACTIVATION)) {
            String isPushOff = (String) installation.get(ParseUpdateUtil.PARSE_ACTIVATION);
            if (TextUtils.equals(isPushOff, ParseUpdateUtil.PARSE_ACTIVATION_OFF)) {
                return;
            }
        }

        installation.put(ParseUpdateUtil.PARSE_ACTIVATION, ParseUpdateUtil.PARSE_ACTIVATION_OFF);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                ColoredToast.show(activity,
                        activity.getString(R.string.jandi_setting_push_subscription_cancel));
            }
        });
    }
}
