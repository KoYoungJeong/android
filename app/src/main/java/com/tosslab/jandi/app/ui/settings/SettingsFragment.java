package com.tosslab.jandi.app.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

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
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.privacy.SettingPrivacyActivity_;
import com.tosslab.jandi.app.ui.settings.push.SettingPushActivity_;
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

        ListPreference settingOrientation = ((ListPreference) getPreferenceManager().findPreference
                ("setting_orientation"));
        String value = settingOrientation.getValue();

        if (TextUtils.isEmpty(value)) {
            settingOrientation.setValue("0");
        }

        settingOrientation.setSummary(SettingsModel.getOrientationSummary(value));

        settingOrientation.setOnPreferenceChangeListener((preference, newValue) -> {

            String value1 = newValue.toString();

            int orientation = SettingsModel.getOrientationValue(value1);
            getActivity().setRequestedOrientation(orientation);

            preference.setSummary(SettingsModel.getOrientationSummary(value1));
            return true;
        });

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

        if (TextUtils.equals(preference.getKey(), "setting_push")) {
            // setting push activity 호출.
            SettingPushActivity_
                    .intent(getActivity())
                    .start();
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
        } else if (TextUtils.equals(preference.getKey(), "setting_set_passcode")) {
            SettingPrivacyActivity_.intent(getActivity())
                    .start();
        } else if (preference.getKey().equals("setting_logout")) {
            if (NetworkCheckUtil.isConnected()) {
                settingFragmentViewModel.showSignoutDialog(getActivity());
            } else {
                settingFragmentViewModel.showCheckNetworkDialog(getActivity());
            }
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.SignOut);

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

}
