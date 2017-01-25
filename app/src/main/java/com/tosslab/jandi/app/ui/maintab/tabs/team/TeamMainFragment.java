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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.tabs.team.adapter.TeamViewPagerAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.info.TeamInfoActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.util.FloatingActionBarDetector;
import com.tosslab.jandi.app.utils.DeviceUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.TabFocusListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Completable;
import rx.schedulers.Schedulers;

public class TeamMainFragment extends Fragment implements TabFocusListener, FloatingActionBarDetector {

    @Bind(R.id.tabs_team_main)
    TabLayout tabLayout;

    @Bind(R.id.viewpager_team_main)
    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        Level myLevel = TeamInfoLoader.getInstance().getMyLevel();
        if (myLevel != Level.Owner && myLevel != Level.Admin) {
            menu.removeItem(R.id.menu_team_config);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_team_config:
                long teamId = TeamInfoLoader.getInstance().getTeamId();
                String url = "https://www.jandi.io/main/#/setting/" + teamId + "/admin/usage";
                startActivity(Henson.with(getContext())
                        .gotoInternalWebActivity()
                        .url(url)
                        .isAdminPage(true)
                        .hasMenu(false)
                        .build()
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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

    @Override
    public void onFocus() {
        FragmentActivity activity = getActivity();

        if (activity != null
                && DeviceUtil.isCallableDevice()
                && (!SdkUtils.hasCanvasPermission() || !SdkUtils.hasPermission(Manifest.permission.CALL_PHONE))
                && JandiPreference.isShowCallPermissionPopup()
                && JandiPreference.isShowCallPermissionToday()) {

            JandiPreference.setShowCallPermissionToday();

            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_call_preview_permission, null);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_call_preview_permission);

            boolean moveSettingBtn;
            if (SdkUtils.isOverMarshmallow()) {
                if (!SdkUtils.hasPermission(Manifest.permission.CALL_PHONE)
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

    @Override
    public void onDetectFloatAction(View btnFab) {
        if (btnFab != null) {
            btnFab.setOnClickListener(v -> {
                InviteDialogExecutor.getInstance().executeInvite(getContext());
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TeamTab, AnalyticsValue.Action.InviteMember);
            });
        }
    }
}
