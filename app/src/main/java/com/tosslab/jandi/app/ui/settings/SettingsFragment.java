package com.tosslab.jandi.app.ui.settings;/**
 * Created by justinygchoi on 2014. 7. 18..
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.settings.viewmodel.SettingFragmentViewModel;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
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

            setPushSubState(isEnabled);
        } else if (TextUtils.equals(preference.getKey(), "setting_tos")) {

            TermActivity_
                    .intent(getActivity())
                    .termMode(TermActivity.Mode.Agreement.name())
                    .start();

        } else if (TextUtils.equals(preference.getKey(), "setting_pp")) {
            TermActivity_
                    .intent(getActivity())
                    .termMode(TermActivity.Mode.Privacy.name())
                    .start();
        } else if (preference.getKey().equals("setting_logout")) {
            settingFragmentViewModel.showSignoutDialog(getActivity());
        }
        return false;
    }

    public void onEvent(SignOutEvent event) {
        trackSignOut();

        startSignOut();
    }

    @Background
    void startSignOut() {
        settingFragmentViewModel.showProgressDialog();
        try {

            removeSignData();

            Activity activity = getActivity();

            ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(activity).getAccountInfo();
            MixpanelAccountAnalyticsClient
                    .getInstance(activity, accountInfo.getId())
                    .trackAccountSigningOut()
                    .flush()
                    .clear();

            EntityManager entityManager = EntityManager.getInstance(activity);

            MixpanelMemberAnalyticsClient
                    .getInstance(activity, entityManager.getDistictId())
                    .trackSignOut()
                    .flush()
                    .clear();

            JandiSocketService.stopService(getActivity());

            BadgeUtils.setBadge(getActivity(), 0);
            ColoredToast.show(getActivity(), getString(R.string.jandi_message_logout));

        } catch (Exception e) {
        } finally {
            settingFragmentViewModel.dismissProgressDialog();
        }

        settingFragmentViewModel.returnToLoginActivity(getActivity());
    }

    private void trackSignOut() {
        Context context = getActivity().getApplicationContext();

        Sprinkler.with(context)
                .track(new FutureTrack.Builder()
                        .event(Event.SignOut)
                        .accountId(AccountUtil.getAccountId(context))
                        .memberId(AccountUtil.getMemberId(context))
                        .build())
                .flush();
    }

    private void removeSignData() {
        JandiPreference.signOut(getActivity());

        ParseUpdateUtil.deleteChannelOnServer();

        JandiAccountDatabaseManager.getInstance(getActivity()).clearAllData();
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
        installation.put(JandiConstants.PARSE_ACTIVATION, JandiConstants.PARSE_ACTIVATION_ON);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                ColoredToast.show(activity
                        , activity.getString(R.string.jandi_setting_push_subscription_ok));
            }
        });
    }

    void offPushNotification() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(JandiConstants.PARSE_ACTIVATION, JandiConstants.PARSE_ACTIVATION_OFF);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                ColoredToast.show(activity
                        , activity.getString(R.string.jandi_setting_push_subscription_cancel));
            }
        });
    }
}
