package com.tosslab.jandi.app.ui.settings.main.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.main.presenter.SettingsPresenter;
import com.tosslab.jandi.app.ui.settings.main.presenter.SettingsPresenterImpl;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.privacy.SettingPrivacyActivity_;
import com.tosslab.jandi.app.ui.settings.push.SettingPushActivity_;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends Fragment implements SettingsPresenter.View {

    @ViewById(R.id.vg_settings_main_orientation)
    SettingsBodyView sbvOrientation;

    @Bean(SettingsPresenterImpl.class)
    SettingsPresenter settingsPresenter;

    private ProgressWheel progressWheel;

    @AfterInject
    void initObjects() {
        settingsPresenter.setView(this);
    }

    @AfterViews
    void initViews() {
        progressWheel = new ProgressWheel(getActivity());
        settingsPresenter.onInitViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Click(R.id.vg_settings_main_notification)
    void onNotificationClick() {
        SettingPushActivity_
                .intent(getActivity())
                .start();
    }

    @Click(R.id.vg_settings_main_term_of_service)
    void onTermServiceClick() {
        TermActivity_
                .intent(getActivity())
                .termMode(TermActivity.Mode.Agreement.name())
                .start();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.TermsOfService);
    }

    @Click(R.id.vg_settings_main_privacy_policy)
    void onPrivacyClick() {
        TermActivity_
                .intent(getActivity())
                .termMode(TermActivity.Mode.Privacy.name())
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.PrivacyPolicy);
    }

    @Click(R.id.vg_settings_main_passcode)
    void onPasscodeClick() {
        SettingPrivacyActivity_.intent(getActivity())
                .start();
    }

    @Click(R.id.vg_settings_main_orientation)
    void onOrientationClick() {
        showOrientationDialog();
    }

    @Click(R.id.vg_settings_main_sign_out)
    void onSignOutClick() {
        settingsPresenter.onSignOut();
    }


    private void showOrientationDialog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String value = sharedPreferences.getString(Settings.SETTING_ORIENTATION, "0");
        String[] values = getResources().getStringArray(R.array.jandi_pref_orientation_values);
        int preselect = Math.max(0, Arrays.asList(values).indexOf(value));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setTitle(R.string.jandi_screen_orientation)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setSingleChoiceItems(R.array.jandi_pref_orientation, preselect, (dialog, which) -> {
                    if (which >= 0 && values != null) {
                        String selectedValue = values[which];
                        sharedPreferences.edit()
                                .putString(Settings.SETTING_ORIENTATION, selectedValue)
                                .commit();

                        settingsPresenter.onSetUpOrientation(selectedValue);
                    }
                    dialog.dismiss();
                });

        builder.create().show();
    }

    @Override
    public void showSignoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage(R.string.jandi_sign_out_message)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_setting_sign_out,
                        (dialog, which) -> EventBus.getDefault().post(new SignOutEvent()))
                .create().show();

    }

    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(getActivity(), null);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressDialog() {
        dismissProgressDialog();

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressDialog() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void setOrientation(int orientation) {
        getActivity().setRequestedOrientation(orientation);
    }

    @Override
    public void setOrientationSummary(String value) {
        sbvOrientation.setSummary(SettingsModel.getOrientationSummary(value));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveLoginActivity() {
        IntroActivity_.intent(SettingsFragment.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    public void onEvent(SignOutEvent event) {

        if (NetworkCheckUtil.isConnected()) {
            trackSignOut();
            settingsPresenter.startSignOut();
        } else {
            showCheckNetworkDialog();
        }
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
