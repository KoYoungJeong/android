package com.tosslab.jandi.app.ui.maintab.tabs.team;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.invites.email.InviteByEmailActivity;
import com.tosslab.jandi.app.ui.invites.emails.InviteEmailActivity;
import com.tosslab.jandi.app.ui.invites.member.MemberInvitationActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.adapter.TeamViewPagerAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.info.TeamInfoActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.DeviceUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.TabFocusListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Completable;
import rx.Observable;
import rx.schedulers.Schedulers;

public class TeamMainFragment extends Fragment implements TabFocusListener {

    @Bind(R.id.tabs_team_main)
    TabLayout tabLayout;

    @Bind(R.id.viewpager_team_main)
    ViewPager viewPager;

    private AlertDialog invitationDialog = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new TeamViewPagerAdapter(getActivity(), getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.MembersTab);
                        break;
                    case 1:
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.DepartmentsTab);
                        break;
                    case 2:
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.JobTitlesTab);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);

                Fragment fragment = ((TeamViewPagerAdapter) viewPager.getAdapter()).getItem(tab.getPosition());
                if (fragment instanceof ListScroller) {
                    ((ListScroller) fragment).scrollToTop();
                }
            }
        });

        viewPager.setCurrentItem(JandiPreference.getLastSelectedTabOfTeam());

        setHasOptionsMenu(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.TeamTab);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Completable.fromAction(() -> JandiPreference.setLastSelectedTabOfTeam(viewPager.getCurrentItem()))
                .subscribeOn(Schedulers.computation())
                .subscribe();

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.team_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_team_add:
//                EventBus.getDefault().post(new RequestInviteMemberEvent(InvitationDialogExecutor.FROM_MAIN_TEAM));
                executeInvite();
                break;
            case R.id.menu_team_info:
                TeamInfoActivity.start(getActivity());
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.TeamInformation);
                break;
            case R.id.menu_team_search:
                startActivity(Henson.with(getActivity())
                        .gotoTeamMemberSearchActivity()
                        .position(viewPager.getCurrentItem())
                        .isSelectMode(false)
                        .from(TeamMemberSearchActivity.EXTRA_FROM_TEAM_TAB)
                        .build());
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.MemberSearch);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private AvailableState availableState(String invitationStatus, String invitationUrl) {
        if (!TextUtils.isEmpty(invitationUrl) && invitationUrl.contains("undefined")) {
            return AvailableState.UNDEFINE;
        }
        if (TextUtils.isEmpty(invitationStatus) || TextUtils.equals(invitationStatus, "disabled")) {
            return AvailableState.DISABLE;
        }
        return AvailableState.AVAIL;
    }

    public void executeInvite() {
        try {
            TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
            boolean teamOwner = teamInfoLoader.getUser(teamInfoLoader.getMyId()).isTeamOwner();
            String invitationStatus = teamInfoLoader.getInvitationStatus();
            String invitationUrl = teamInfoLoader.getInvitationUrl();
            AvailableState availableState = availableState(invitationStatus, invitationUrl);
            switch (availableState) {
                case AVAIL:
                    showInvitationDialog();
                    break;
                case UNDEFINE:
                    if (!teamOwner) {
                        showErrorToast(JandiApplication.getContext()
                                .getString(R.string.err_entity_invite));
                    }
                    break;
                case DISABLE:
                    if (!teamOwner) {
                        showErrorInviteDisabledDialog();
                    }
                    break;
            }

            if (teamOwner && availableState != AvailableState.AVAIL) {
                ColoredToast.showGray(R.string.jandi_invitation_for_admin);
                Intent intent = new Intent(getActivity(), InviteByEmailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_entity_invite));
        }
    }

    public void showInvitationDialog() {
        if (invitationDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300);

            android.view.View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_invitation_user, null);

            view.findViewById(R.id.vg_invite_associate)
                    .setOnClickListener(v -> {
                        if (hasNonDefaultTopic()) {
                            InviteEmailActivity.startActivityForAssociate(getActivity());
                            invitationDialog.dismiss();
                        } else {
                            showErrorNotAvailableInviteTopicDialog();
                        }
                    });

            view.findViewById(R.id.vg_invite_member)
                    .setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), MemberInvitationActivity.class);
                        startActivity(intent);
                        invitationDialog.dismiss();
                    });

            invitationDialog = builder
                    .setTitle(JandiApplication.getContext().getString(R.string.invite_member_option_title))
                    .setView(view)
                    .setNegativeButton(this.getResources().getString(R.string.jandi_cancel),
                            (dialog, id) -> dialog.dismiss())
                    .create();
        }

        if (!invitationDialog.isShowing()) {
            invitationDialog.show();
        }
    }

    @Override
    public void onFocus() {
        FragmentActivity activity = getActivity();

        if (activity != null
                && DeviceUtil.isCallableDevice()
                && JandiPreference.isShowCallPermissionPopup()) {

            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_call_preview_permission, null);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_call_preview_permission);

            boolean moveSettingBtn;
            if (SdkUtils.isMarshmallow()) {
                if (!SdkUtils.hasPermission(activity, Manifest.permission.CALL_PHONE)
                        || !Settings.canDrawOverlays(activity)) {
                    moveSettingBtn = true;
                } else {
                    checkBox.setVisibility(View.GONE);
                    moveSettingBtn = false;
                    JandiPreference.setShowCallPermissionPopup();
                }
            } else {
                checkBox.setVisibility(View.GONE);
                moveSettingBtn = false;
                JandiPreference.setShowCallPermissionPopup();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300);
            builder.setView(view).setNegativeButton(R.string.common_calleridnotifier_closemodal, null);
            if (moveSettingBtn) {
                builder.setPositiveButton(R.string.common_calleridnotifier_tosettings, (dialog, which) -> {
                    startActivity(Henson.with(activity)
                            .gotoCallSettingActivity()
                            .build());
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.Whoscall_MoveToSetting);
                });
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.Whoscall_DontShowAgain, AnalyticsValue.Label.On);
                } else {
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.Whoscall_DontShowAgain, AnalyticsValue.Label.Off);
                }
            });

            builder.setOnDismissListener(dialog -> {
                if (checkBox.isChecked()) {
                    JandiPreference.setShowCallPermissionPopup();
                }
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.Whoscall_Close);
            }).create().show();

        }
    }

    private boolean hasNonDefaultTopic() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(topic -> topic.isJoined())
                .filter(topic -> !topic.isDefaultTopic())
                .count()
                .map(cnt -> cnt > 0)
                .toBlocking()
                .firstOrDefault(false);
    }

    private String getOwnerName() {
        List<User> users = TeamInfoLoader.getInstance().getUserList();
        return Observable.from(users)
                .filter(User::isTeamOwner)
                .map(User::getName)
                .toBlocking()
                .firstOrDefault("");
    }

    private void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    private void showErrorInviteDisabledDialog() {
        new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(JandiApplication.getContext()
                        .getString(R.string.jandi_invite_disabled, getOwnerName()))
                .setCancelable(false)
                .setPositiveButton(getActivity().getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                            AnalyticsUtil.sendEvent(
                                    AnalyticsValue.Screen.TeamTab,
                                    AnalyticsValue.Action.InviteMember_InviteDisabled);
                            dialog.dismiss();
                        })
                .create().show();
    }

    private void showErrorNotAvailableInviteTopicDialog() {
        new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(JandiApplication.getContext()
                        .getString(R.string.invite_associate_invitoronlyindefault_title))
                .setMessage(JandiApplication.getContext()
                        .getString(R.string.invite_associate_invitoronlyindefault_desc))
                .setCancelable(false)
                .setPositiveButton(getActivity().getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                        })
                .create().show();
    }

    private enum AvailableState {
        AVAIL, UNDEFINE, DISABLE
    }

}
