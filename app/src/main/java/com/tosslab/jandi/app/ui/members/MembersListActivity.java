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

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.members.adapter.ModdableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.kick.KickDialogFragment;
import com.tosslab.jandi.app.ui.members.kick.KickDialogFragment_;
import com.tosslab.jandi.app.ui.members.owner.AssignTopicOwnerDialog;
import com.tosslab.jandi.app.ui.members.owner.AssignTopicOwnerDialog_;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;

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

    private ModdableMemberListAdapter topicModdableMemberListAdapter;

    @AfterInject
    void initObject() {
        int ownerType = (type == TYPE_MEMBERS_LIST_TOPIC || type == TYPE_ASSIGN_TOPIC_OWNER)
                ? ModdableMemberListAdapter.OWNER_TYPE_TOPIC : ModdableMemberListAdapter.OWNER_TYPE_TEAM;
        topicModdableMemberListAdapter = new ModdableMemberListAdapter(ownerType);
        if (type == TYPE_ASSIGN_TOPIC_OWNER) {
            topicModdableMemberListAdapter.setOnMemberClickListener(item ->
                    membersListPresenter.onMemberClickForAssignOwner(entityId, item));
        }

        membersListPresenter.setView(this);
    }

    private AnalyticsValue.Screen getScreen() {
        switch (type) {
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

        SprinklrScreenView.sendLog(ScreenViewProperty.TEAM_MEMBER);

        AnalyticsUtil.sendScreenName(getScreen());

        setupActionbar();

        memberListView.setLayoutManager(new LinearLayoutManager(MembersListActivity.this,
                RecyclerView.VERTICAL, false));
        memberListView.setAdapter(topicModdableMemberListAdapter);
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
        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.SearchInputField);
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
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.team_info_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_invitation);
        if (type == TYPE_MEMBERS_LIST_TOPIC) {
            menuItem.setVisible(false);
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
        final List<Long> selectedUserIds = topicModdableMemberListAdapter.getSelectedUserIds();

        topicModdableMemberListAdapter.clear();

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
        topicModdableMemberListAdapter.addAll(members);
        topicModdableMemberListAdapter.notifyDataSetChanged();
        if (type == TYPE_MEMBERS_LIST_TEAM
                && tvSearch.getText().length() <= 0
                && topicModdableMemberListAdapter.getCount() <= 1) {
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
    public void moveDirectMessageActivity(long teamId, long userId) {
        MessageListV2Activity_.intent(MembersListActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .roomId(-1)
                .entityId(userId)
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
        topicModdableMemberListAdapter.setKickMode(owner);

        if (owner) {
            topicModdableMemberListAdapter.setOnKickClickListener((adapter, viewHolder, position) -> {
                ChatChooseItem item = ((ModdableMemberListAdapter) adapter).getItem(position);
                membersListPresenter.onKickMemberClick(entityId, item);
            });
        }

        topicModdableMemberListAdapter.notifyDataSetChanged();


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
        for (int idx = 0, size = topicModdableMemberListAdapter.getCount(); idx < size; idx++) {
            if (topicModdableMemberListAdapter.getItem(idx).getEntityId() == userEntityId) {
                topicModdableMemberListAdapter.remove(idx);
                topicModdableMemberListAdapter.notifyDataSetChanged();
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