package com.tosslab.jandi.app.ui.settings.main.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.account.SettingAccountActivity;
import com.tosslab.jandi.app.ui.settings.main.dagger.DaggerSettingsComponent;
import com.tosslab.jandi.app.ui.settings.main.dagger.SettingsModule;
import com.tosslab.jandi.app.ui.settings.main.presenter.SettingsPresenter;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.privacy.SettingPrivacyActivity_;
import com.tosslab.jandi.app.ui.settings.push.SettingPushActivity_;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignOut;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class SettingsFragment extends Fragment implements SettingsPresenter.View {

    @Bind(R.id.vg_settings_main_orientation_wrapper)
    ViewGroup vgOrientation;

    @Bind(R.id.vg_settings_main_orientation)
    SettingsBodyView sbvOrientation;

    @Bind(R.id.vg_settings_main_version)
    SettingsBodyView sbvVersion;

    @Inject
    SettingsPresenter settingsPresenter;

    private ProgressWheel progressWheel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        DaggerSettingsComponent.builder()
                .settingsModule(new SettingsModule(this))
                .build()
                .inject(this);
        initViews();
    }

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

    @OnClick(R.id.vg_settings_main_notification)
    void onNotificationClick() {
        SettingPushActivity_
                .intent(getActivity())
                .start();
    }

    @OnClick(R.id.vg_settings_main_term_of_service)
    void onTermServiceClick() {
        startActivity(new Intent(getActivity(), TermActivity.class)
                .putExtra(TermActivity.EXTRA_TERM_MODE, TermActivity.Mode.Agreement.name()));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.TermsOfService);
    }

    @OnClick(R.id.vg_settings_main_privacy_policy)
    void onPrivacyClick() {
        startActivity(new Intent(getActivity(), TermActivity.class)
                .putExtra(TermActivity.EXTRA_TERM_MODE, TermActivity.Mode.Privacy.name()));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.PrivacyPolicy);
    }

    @OnClick(R.id.vg_settings_main_passcode)
    void onPasscodeClick() {
        SettingPrivacyActivity_.intent(getActivity())
                .start();
    }

    @OnClick(R.id.vg_settings_main_orientation)
    void onOrientationClick() {
        showOrientationDialog();
    }

    @OnClick(R.id.vg_settings_main_account_name)
    void onAccountClick() {
        Intent intent = new Intent(getActivity(), SettingAccountActivity.class);
        startActivity(intent);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.Account);
    }

    @OnClick(R.id.vg_settings_main_sign_out)
    void onSignOutClick() {
        settingsPresenter.onSignOut();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.SignOut);
    }

    @OnClick(R.id.vg_settings_main_help)
    void onHelpClick() {
        settingsPresenter.onLaunchHelpPage();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.Help);
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
    public void showSignOutDialog() {
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

    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void showProgressDialog() {
        dismissProgressDialog();

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void setOrientationViewVisibility(boolean show) {
        vgOrientation.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOrientation(int orientation) {
        getActivity().setRequestedOrientation(orientation);
    }

    @Override
    public void setOrientationSummary(String value) {
        sbvOrientation.setSummary(SettingsModel.getOrientationSummary(value));
    }

    @Override
    public void moveLoginActivity() {
        IntroActivity.startActivity(getActivity(), false);
    }

    @Override
    public void setVersion(String version) {
        sbvVersion.setTitle(version);
    }

    @Override
    public void launchHelpPage(String supportUrl) {
        ApplicationUtil.startWebBrowser(getActivity(), supportUrl);
    }

    public void onEvent(SignOutEvent event) {
        SprinklrSignOut.sendLog();
        AnalyticsUtil.flushSprinkler();
        settingsPresenter.startSignOut();
    }

}
