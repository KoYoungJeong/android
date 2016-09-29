package com.tosslab.jandi.app.ui.maintab.navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.android.pushservice.PushSettings;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.team.TeamDeletedEvent;
import com.tosslab.jandi.app.events.team.TeamInfoChangeEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteAcceptEvent;
import com.tosslab.jandi.app.events.team.invite.TeamInviteIgnoreEvent;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.services.socket.to.MessageReadEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.maintab.dialog.UsageInformationDialogFragment_;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.NavigationAdapter;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.view.NavigationDataView;
import com.tosslab.jandi.app.ui.maintab.navigation.component.DaggerNavigationComponent;
import com.tosslab.jandi.app.ui.maintab.navigation.module.NavigationModule;
import com.tosslab.jandi.app.ui.maintab.navigation.presenter.NavigationPresenter;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.account.SettingAccountActivity;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.privacy.SettingPrivacyActivity_;
import com.tosslab.jandi.app.ui.settings.push.SettingPushActivity_;
import com.tosslab.jandi.app.ui.team.create.CreateTeamActivity;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.KnockListener;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignOut;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UnreadConversationCountListener;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public class NavigationFragment extends Fragment implements NavigationPresenter.View {

    private static final int REQUEST_TEAM_CREATE = 1603;

    @Bind(R.id.iv_navigation_profile_large)
    ImageView ivProfileLarge;
    @Bind(R.id.iv_navigation_profile)
    ImageView ivProfile;
    @Bind(R.id.v_navigation_profile_large_overlay)
    View vProfileImageLargeOverlay;
    @Bind(R.id.tv_navigation_profile_name)
    TextView tvName;
    @Bind(R.id.tv_navigation_profile_email)
    TextView tvEmail;
    @Bind(R.id.v_navigation_owner_badge)
    View vOwnerBadge;

    @Bind(R.id.lv_navigation)
    RecyclerView lvNavigation;

    @Inject
    NavigationPresenter navigationPresenter;

    @Inject
    NavigationDataView navigationDataView;

    private ProgressWheel progressWheel;
    private KnockListener usageInformationKnockListener;
    private UnreadConversationCountListener intercomUnreadCountListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        NavigationAdapter navigationAdapter = new NavigationAdapter();
        injectComponent(navigationAdapter);

        initNavigationListViews(navigationAdapter);

        initProgressWheel();

        initNavigations();

        initUsageInformationKnockListener();

        navigationPresenter.onInitIntercom();

        Intercom.client().addUnreadConversationCountListener(intercomUnreadCountListener = count -> {
            notifyDataSetChanged();
            navigationPresenter.initBadgeCount();
        });
    }

    @Override
    public void onDestroy() {
        if (intercomUnreadCountListener != null) {
            Intercom.client().removeUnreadConversationCountListener(intercomUnreadCountListener);
        }
        super.onDestroy();
    }

    void initNavigations() {
        navigationPresenter.onInitUserProfile();
        navigationPresenter.onInitializePresetNavigationItems();
        navigationPresenter.onInitializeTeams();
    }

    void initProgressWheel() {
        progressWheel = new ProgressWheel(getActivity());
    }

    void injectComponent(NavigationAdapter navigationAdapter) {
        DaggerNavigationComponent.builder()
                .navigationModule(new NavigationModule(navigationAdapter, this))
                .build()
                .inject(this);
    }

    void initNavigationListViews(NavigationAdapter navigationAdapter) {
        lvNavigation.setLayoutManager(new LinearLayoutManager(getContext()));
        lvNavigation.setAdapter(navigationAdapter);

        navigationDataView.setOnNavigationItemClickListener(this::onOptionsItemSelected);
        navigationDataView.setOnRequestTeamCreateListener(this::moveToTeamCreate);
        navigationDataView.setOnTeamClickListener(this::joinToTeam);
        navigationDataView.setOnVersionClickListener(() -> usageInformationKnockListener.knock());
    }

    private void joinToTeam(Team team) {
        navigationPresenter.onTeamJoinAction(team.getTeamId());

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.ChooseTeam);
    }

    private void moveToTeamCreate() {
        Intent intent = new Intent(getActivity(), CreateTeamActivity.class);
        startActivityForResult(intent, REQUEST_TEAM_CREATE);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.CreateNewTeam);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_setting_notification:
                moveToSetUpNotification();
                return true;
            case R.id.nav_setting_passcode:
                moveToSetUpPasscode();
                return true;
            case R.id.nav_setting_orientation:
                showSettingOrientationDialog();
                return true;
            case R.id.nav_setting_account:
                moveToSetUpAccount();
                return true;
            case R.id.nav_term_of_service:
                moveToCheckTeamsOfService();
                return true;
            case R.id.nav_privacy_policy:
                moveToCheckPrivacyPolicy();
                return true;
            case R.id.nav_help:
                moveToShowHelpPage();
                return true;
            case R.id.nav_1_on_1:
                moveToLiveSupport();
                return true;
            case R.id.nav_sign_out:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void moveToLiveSupport() {
        Intercom.client().displayMessenger();
    }

    private void moveToShowHelpPage() {
        navigationPresenter.onLaunchHelpPage();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.Help);
    }

    private void signOut() {
        showSignOutDialog();

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.SignOut);
    }

    private void moveToCheckPrivacyPolicy() {
        startActivity(new Intent(getActivity(), TermActivity.class)
                .putExtra(TermActivity.EXTRA_TERM_MODE, TermActivity.Mode.Privacy.name()));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.PrivacyPolicy);
    }

    private void moveToCheckTeamsOfService() {
        startActivity(new Intent(getActivity(), TermActivity.class)
                .putExtra(TermActivity.EXTRA_TERM_MODE, TermActivity.Mode.Agreement.name()));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.TermsOfService);
    }

    private void moveToSetUpAccount() {
        Intent intent = new Intent(getActivity(), SettingAccountActivity.class);
        startActivity(intent);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, AnalyticsValue.Action.Account);
    }

    private void moveToSetUpPasscode() {
        SettingPrivacyActivity_.intent(getActivity())
                .start();
    }

    private void moveToSetUpNotification() {
        SettingPushActivity_
                .intent(getActivity())
                .start();
    }

    @SuppressLint("CommitPrefEdits")
    private void showSettingOrientationDialog() {
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
                        int orientation = SettingsModel.getOrientationValue(selectedValue);
                        getActivity().setRequestedOrientation(orientation);
                    }
                    dialog.dismiss();
                });

        builder.create().show();
    }

    private void showSignOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage(R.string.jandi_sign_out_message)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_setting_sign_out,
                        (dialog, which) -> {
                            navigationPresenter.onSignOutAction();
                            SprinklrSignOut.sendLog();
                            AnalyticsUtil.flushSprinkler();
                        })
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
    public void moveLoginActivity() {
        IntroActivity.startActivity(getActivity(), false);
    }

    @Override
    public void launchHelpPage(String supportUrl) {
        ApplicationUtil.startWebBrowser(getActivity(), supportUrl);
    }

    @Override
    public void moveToSelectTeam() {
        JandiSocketService.stopService(getActivity());
        getActivity().sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        startActivity(Henson.with(getActivity())
                .gotoMainTabActivity()
                .isLoadInitialInfo(true)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        getActivity().overridePendingTransition(0, 0);
        getActivity().finish();
    }

    public void onEvent(TeamInviteIgnoreEvent event) {
        navigationPresenter.onTeamInviteIgnoreAction(event.getTeam());
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.AcceptTeamInvitation);
    }

    public void onEvent(TeamInviteAcceptEvent event) {
        navigationPresenter.onTeamInviteAcceptAction(event.getTeam());
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SwitchTeam, AnalyticsValue.Action.IgnoreTeamInvitation);
    }

    public void onEvent(TeamDeletedEvent event) {
        navigationPresenter.onInitializeTeams();
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (event.isConnected()) {
            navigationPresenter.onInitializeTeams();
        }
    }

    @Override
    public void showTeamInviteIgnoreFailToast(String errorMessage) {
        ColoredToast.showError(errorMessage);
    }

    @Override
    public void showTeamInviteAcceptFailDialog(String errorMessage, Team team) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        AlertUtil.showConfirmDialog(getActivity(), errorMessage, (dialog, which) -> {
            navigationPresenter.onTeamInviteIgnoreAction(team);
        }, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEAM_CREATE) {
            onTeamCreateResult(resultCode);
        }
    }

    void onTeamCreateResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            navigationPresenter.onTeamCreated();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        navigationDataView.notifyDataSetChanged();
    }

    @Override
    public void setUserProfile(User user) {
        if (user == null || user.getId() <= 0) {
            return;
        }

        String photoUrl = user.getPhotoUrl();
        ImageUtil.loadProfileImage(ivProfile, photoUrl, R.drawable.profile_img);

        Resources resources = ivProfile.getResources();
        int defaultColor = resources.getColor(R.color.jandi_member_profile_img_overlay_default);
        if (!TextUtils.isEmpty(photoUrl) && photoUrl.contains("files-profile")) {
            Drawable placeHolder = new ColorDrawable(defaultColor);
            ImageLoader.newInstance()
                    .placeHolder(placeHolder, ImageView.ScaleType.FIT_XY)
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .transformation(new BlurTransformation(ivProfile.getContext(), 50))
                    .uri(Uri.parse(ImageUtil.getLargeProfileUrl(photoUrl)))
                    .into(ivProfileLarge);
        } else {
            vProfileImageLargeOverlay.setBackgroundColor(defaultColor);
        }

        ivProfile.setOnClickListener(v -> moveToProfileSettingActivity());
        tvName.setText(user.getName());
        vOwnerBadge.setVisibility(user.isTeamOwner() ? View.VISIBLE : View.GONE);
        tvEmail.setText(user.getEmail());
        easterEggForLog(tvEmail);
    }

    @Override
    public void closeNavigation() {
        if (getActivity() != null && getActivity() instanceof NavigationOwner) {
            ((NavigationOwner) getActivity()).closeNavigation();
        }
    }

    private void easterEggForLog(View view) {
        KnockListener knockListener = KnockListener.create()
                .expectKnockCount(10)
                .expectKnockedIn(3000)
                .onKnocked(() -> {
                    LogUtil.LOG = true;
                    PushSettings.enableDebugMode(JandiApplication.getContext(), LogUtil.LOG);
                });
        view.setOnClickListener(v -> knockListener.knock());
    }

    private void moveToProfileSettingActivity() {
        Intent intent = new Intent(getActivity(), ModifyProfileActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onEventMainThread(ProfileChangeEvent event) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (event.getMember().getId() == TeamInfoLoader.getInstance().getMyId()) {
            navigationPresenter.onInitUserProfile();
        }
    }

    public void onEvent(TeamInfoChangeEvent event) {
        navigationPresenter.onInitializeTeams();
    }

    public void onEvent(SocketMessageDeletedEvent event) {
        navigationPresenter.onMessageDeleted(event.getTeamId());
    }

    public void onEvent(SocketMessageCreatedEvent event) {
        navigationPresenter.onMessageCreated(event.getTeamId());
    }

    public void onEvent(MessageReadEvent event) {
        navigationPresenter.onMessageRead(event.fromSelf(), event.getTeamId(), event.getReadCount());
    }

    private void initUsageInformationKnockListener() {
        usageInformationKnockListener = KnockListener.create()
                .expectKnockCount(10)
                .expectKnockedIn(5000)
                .onKnocked(this::showBugReportDialog);
    }

    private void showBugReportDialog() {
        UsageInformationDialogFragment_.builder().build()
                .show(getFragmentManager(), "usageInformationKnock");
    }

    @Override
    public void onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        navigationPresenter.clearTeamInitializeQueue();
        navigationPresenter.clearBadgeCountingQueue();
        super.onDestroyView();
    }

    public interface NavigationOwner {
        void openNavigation();

        void closeNavigation();
    }

}
