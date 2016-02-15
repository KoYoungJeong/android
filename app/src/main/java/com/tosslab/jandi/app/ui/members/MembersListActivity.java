package com.tosslab.jandi.app.ui.members;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.members.adapter.MembersAdapter;
import com.tosslab.jandi.app.ui.members.kick.KickDialogFragment;
import com.tosslab.jandi.app.ui.members.kick.KickDialogFragment_;
import com.tosslab.jandi.app.ui.members.owner.AssignTopicOwnerDialog;
import com.tosslab.jandi.app.ui.members.owner.AssignTopicOwnerDialog_;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.Observable;

/**
 * Created by tee on 15. 6. 3..
 */

@EActivity(R.layout.activity_topic_member)
public class MembersListActivity extends BaseAppCompatActivity implements MembersListPresenter.View {

    public static final int TYPE_MEMBERS_LIST_TEAM = 1;
    public static final int TYPE_MEMBERS_LIST_TOPIC = 2;
    public static final int TYPE_MEMBERS_JOINABLE_TOPIC = 3;
    public static final int TYPE_ASSIGN_TOPIC_OWNER = 4;
    public static final String KEY_MEMBER_ID = "memberId";

    @Extra
    long entityId;

    @Extra
    int type;

    @Bean(MembersListPresenterImpl.class)
    MembersListPresenter membersListPresenter;

    @ViewById(R.id.list_topic_member)
    RecyclerView memberListView;

    @ViewById(R.id.vg_topic_member_search_bar)
    View vgSearchbar;

    @ViewById(R.id.et_topic_member_search)
    TextView tvSearch;

    @ViewById(R.id.vg_team_member_empty)
    View vEmptyTeamMember;

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;

    private ProgressWheel mProgressWheel;

    private MembersAdapter topicMembersAdapter;

    @AfterInject
    void initObject() {
        int ownerType = (type == TYPE_MEMBERS_LIST_TOPIC || type == TYPE_ASSIGN_TOPIC_OWNER)
                ? MembersAdapter.OWNER_TYPE_TOPIC : MembersAdapter.OWNER_TYPE_TEAM;
        topicMembersAdapter = new MembersAdapter(getBaseContext(), ownerType);
        if (type == TYPE_MEMBERS_JOINABLE_TOPIC) {
            topicMembersAdapter.setCheckMode();
        } else if (type == TYPE_ASSIGN_TOPIC_OWNER) {
            topicMembersAdapter.setOnMemberClickListener(item ->
                    membersListPresenter.onMemberClickForAssignOwner(entityId, item));
        }

        membersListPresenter.setView(this);
    }

    private AnalyticsValue.Screen getScreen() {
        switch (type) {
            case TYPE_MEMBERS_JOINABLE_TOPIC:
                return AnalyticsValue.Screen.InviteTeamMembers;
            default:
            case TYPE_MEMBERS_LIST_TEAM:
                return AnalyticsValue.Screen.TeamMembers;
            case TYPE_MEMBERS_LIST_TOPIC:
                return AnalyticsValue.Screen.Participants;
        }
    }

    @AfterViews
    void initViews() {
        vEmptyTeamMember.setVisibility(View.GONE);

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.TEAM_MEMBER)
                        .build());

        AnalyticsUtil.sendScreenName(getScreen());

        setupActionbar();

        memberListView.setLayoutManager(new LinearLayoutManager(MembersListActivity.this,
                RecyclerView.VERTICAL, false));
        if (type != TYPE_MEMBERS_JOINABLE_TOPIC) {
            memberListView.addItemDecoration(new SimpleDividerItemDecoration(MembersListActivity.this));
        }
        memberListView.setAdapter(topicMembersAdapter);
        initProgressWheel();

        memberListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private boolean initialize = true;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (initialize) {
                    initialize = false;
                    return;
                }

                final float translateY = vgSearchbar.getTranslationY() - dy;

                float futureTranslateY = Math.max(-vgSearchbar.getMeasuredHeight(), translateY);
                vgSearchbar.setTranslationY(Math.min(0, futureTranslateY));
            }
        });

        if (type == TYPE_MEMBERS_LIST_TOPIC) {
            membersListPresenter.initKickableMode(entityId);
        }


    }

    @TextChange(R.id.et_topic_member_search)
    void onSearchTextChange(CharSequence text) {
        membersListPresenter.onSearch(text);
    }


    @Click(R.id.et_topic_member_search)
    void onSearchInputClick() {
        if (type == TYPE_MEMBERS_JOINABLE_TOPIC) {
            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.SearchInviteMember);
        } else {
            AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.SearchInputField);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        membersListPresenter.onDestroy();
    }

    private int getActionbarHeight() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);

        return TypedValue.complexToDimensionPixelOffset(
                typedValue.data, getResources().getDisplayMetrics());
    }

    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        if (type == TYPE_MEMBERS_LIST_TEAM) {
            actionBar.setTitle(R.string.jandi_team_member);
        } else if (type == TYPE_MEMBERS_LIST_TOPIC) {
            actionBar.setTitle(R.string.jandi_topic_paricipants);
        } else if (type == TYPE_MEMBERS_JOINABLE_TOPIC) {
            actionBar.setTitle(R.string.jandi_invite_member_to_topic);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.team_info_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_invitation);
        if (type == TYPE_MEMBERS_LIST_TOPIC) {
            menuItem.setVisible(false);
        } else if (type == TYPE_MEMBERS_JOINABLE_TOPIC) {
            menuItem.setIcon(R.drawable.icon_actionbar_check);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    List<Long> selectedCdp = topicMembersAdapter.getSelectedUserIds();

                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteTeamMembers, AnalyticsValue.Action.Invite);

                    if (selectedCdp != null && !selectedCdp.isEmpty()) {
                        membersListPresenter.inviteInBackground(selectedCdp, entityId);
                        finish();
                        return true;
                    } else {
                        showInviteFailed(getString(R.string.title_cdp_invite));
                        return false;
                    }
                }
            });
        } else if (type == TYPE_ASSIGN_TOPIC_OWNER) {
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        membersListPresenter.onEventBusRegister();
        ActivityHelper.setOrientation(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        membersListPresenter.onEventBusUnregister();
    }

    @SupposeUiThread
    void initProgressWheel() {
        mProgressWheel = new ProgressWheel(MembersListActivity.this);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        dismissProgressWheel();

        if (mProgressWheel == null) {
            mProgressWheel = new ProgressWheel(MembersListActivity.this);
        }

        if (!mProgressWheel.isShowing()) {
            mProgressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }

    @OptionsItem(R.id.action_invitation)
    void onInviteOptionSelect() {
        if (type == TYPE_MEMBERS_LIST_TEAM) {
            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_MAIN_MEMBER);
            invitationDialogExecutor.execute();
        } else if (type == TYPE_MEMBERS_LIST_TOPIC) {
            membersListPresenter.inviteMemberToTopic(entityId);
        }
    }

    @Click(value = {
            R.id.img_chat_choose_member_empty,
            R.id.btn_chat_choose_member_empty})
    void onMemberJoinClick() {
        onInviteOptionSelect();
    }

    @UiThread
    @Override
    public void showListMembers(List<ChatChooseItem> members) {
        final List<Long> selectedUserIds = topicMembersAdapter.getSelectedUserIds();

        topicMembersAdapter.clear();

        if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
            Observable.from(members)
                    .filter(member -> {
                        for (long ids : selectedUserIds) {
                            boolean selected = ids == member.getEntityId();
                            if (selected) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .subscribe(member -> {
                        member.setIsChooseItem(true);
                    });
        }
        topicMembersAdapter.addAll(members);
        topicMembersAdapter.notifyDataSetChanged();
        if (type == TYPE_MEMBERS_LIST_TEAM
                && tvSearch.getText().length() <= 0
                && topicMembersAdapter.getCount() <= 1) {
            // 팀 멤버 검색 && 검색어 없음 && 나를 포함해서 1명이하인 경우
            vEmptyTeamMember.setVisibility(View.VISIBLE);
        } else {
            vEmptyTeamMember.setVisibility(View.GONE);
        }
    }

    @Override
    public long getEntityId() {
        return entityId;
    }

    @Override
    public int getType() {
        return type;
    }

    @UiThread
    @Override
    public void moveDirectMessageActivity(long teamId, long userId, boolean isStarred) {
        MessageListV2Activity_.intent(MembersListActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .roomId(-1)
                .entityId(userId)
                .isFavorite(isStarred)
                .isFromPush(false)
                .start();
    }

    @Override
    public String getSearchText() {
        return tvSearch.getText().toString();
    }

    @Override
    @UiThread
    public void showInviteSucceed(int memberSize) {
        String rawString = getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);
        ColoredToast.show(formatString);
    }

    @Override
    @UiThread
    public void showInviteFailed(String errMessage) {
        ColoredToast.showError(errMessage);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setKickMode(boolean owner) {
        topicMembersAdapter.setKickMode(owner);

        if (owner) {
            topicMembersAdapter.setOnKickClickListener((adapter, viewHolder, position) -> {
                ChatChooseItem item = ((MembersAdapter) adapter).getItem(position);
                membersListPresenter.onKickMemberClick(entityId, item);
            });
        }

        topicMembersAdapter.notifyDataSetChanged();


    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showKickDialog(String userName, String userProfileUrl, long memberId) {
        KickDialogFragment dialogFragment = KickDialogFragment_.builder()
                .profileUrl(userProfileUrl)
                .userName(userName)
                .build();

        dialogFragment.setOnKickConfirmClickListener((dialog, which) -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Participants, AnalyticsValue.Action.KickMember);
            membersListPresenter.onKickUser(entityId, memberId);
        });

        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void removeUser(long userEntityId) {
        for (int idx = 0, size = topicMembersAdapter.getCount(); idx < size; idx++) {
            if (topicMembersAdapter.getItem(idx).getEntityId() == userEntityId) {
                topicMembersAdapter.remove(idx);
                topicMembersAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void refreshMemberList() {
        membersListPresenter.onSearch(tvSearch.getText());
    }

    @UiThread
    @Override
    public void showKickSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_success_kick_user_from_topic));
    }

    @UiThread
    @Override
    public void showKickFailToast() {
        ColoredToast.show(getString(R.string.jandi_err_unexpected));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showAlreadyTopicOwnerToast() {
        ColoredToast.showError(getString(R.string.jandi_alert_already_topic_owenr));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showNeedToAssignTopicOwnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MembersListActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_topic_owner_title);
        builder.setMessage(R.string.jandi_guide_to_assign_topic_owner);
        builder.setPositiveButton(R.string.jandi_confirm, null);
        builder.create().show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showConfirmAssignTopicOwnerDialog(String userName, String userProfileUrl, long memberId) {
        AssignTopicOwnerDialog assignDialog = AssignTopicOwnerDialog_.builder()
                .profileUrl(userProfileUrl)
                .userName(userName)
                .build();
        assignDialog.setConfirmListener((dialog, which) -> {
            membersListPresenter.onAssignToTopicOwner(entityId, memberId);
        });
        assignDialog.show(getSupportFragmentManager(), "assign_dialog");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showAssignTopicOwnerSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_complete_assign_topic_owner));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showAssignTopicOwnerFailToast() {
        ColoredToast.showError(getString(R.string.jandi_err_unexpected));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setResultAndFinish(long memberId) {
        if (memberId > 0) {
            Intent intent = new Intent();
            intent.putExtra(KEY_MEMBER_ID, memberId);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

}