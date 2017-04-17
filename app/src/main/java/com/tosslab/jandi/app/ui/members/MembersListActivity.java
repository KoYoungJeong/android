package com.tosslab.jandi.app.ui.members;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;
import com.tosslab.jandi.app.ui.members.adapter.ModdableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.dagger.DaggerMemberListComponent;
import com.tosslab.jandi.app.ui.members.dagger.MemberListModule;
import com.tosslab.jandi.app.ui.members.kick.KickDialogFragment;
import com.tosslab.jandi.app.ui.members.owner.AssignTopicOwnerDialog;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.utils.AccessLevelUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class MembersListActivity extends BaseAppCompatActivity implements MembersListPresenter.View {

    public static final int TYPE_MEMBERS_LIST_TEAM = 1;
    public static final int TYPE_MEMBERS_LIST_TOPIC = 2;
    public static final int TYPE_ASSIGN_TOPIC_OWNER = 4;
    public static final String KEY_MEMBER_ID = "memberId";

    @InjectExtra
    long entityId;

    @InjectExtra
    int type;

    @Inject
    MembersListPresenter membersListPresenter;

    @Bind(R.id.list_topic_member)
    RecyclerView memberListView;

    @Bind(R.id.vg_topic_member_search_bar)
    View vgSearchbar;

    @Bind(R.id.et_topic_member_search)
    TextView tvSearch;

    @Bind(R.id.vg_team_member_empty)
    View vEmptyTeamMember;

    private ProgressWheel mProgressWheel;

    private ModdableMemberListAdapter topicModdableMemberListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_member);
        Dart.inject(this);
        ButterKnife.bind(this);

        initObject();
        initViews();
        membersListPresenter.onInit();
    }

    void initObject() {
        DaggerMemberListComponent.builder().memberListModule(new MemberListModule(this))
                .build().inject(this);

        int ownerType = (type == TYPE_MEMBERS_LIST_TOPIC || type == TYPE_ASSIGN_TOPIC_OWNER)
                ? ModdableMemberListAdapter.OWNER_TYPE_TOPIC : ModdableMemberListAdapter.OWNER_TYPE_TEAM;
        topicModdableMemberListAdapter = new ModdableMemberListAdapter(ownerType);
        if (type == TYPE_ASSIGN_TOPIC_OWNER) {
            topicModdableMemberListAdapter.setOnMemberClickListener(item ->
                    membersListPresenter.onMemberClickForAssignOwner(entityId, item));
        }
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

    void initViews() {
        vEmptyTeamMember.setVisibility(View.GONE);

        SprinklrScreenView.sendLog(ScreenViewProperty.TEAM_MEMBER);

        AnalyticsUtil.sendScreenName(getScreen());

        setupActionbar();

        memberListView.setLayoutManager(new LinearLayoutManager(MembersListActivity.this,
                RecyclerView.VERTICAL, false));
        memberListView.setAdapter(topicModdableMemberListAdapter);
        initProgressWheel();

        if (type == TYPE_MEMBERS_LIST_TOPIC) {
            membersListPresenter.initKickableMode(entityId);
        }

    }

    @OnTextChanged(R.id.et_topic_member_search)
    void onSearchTextChange(CharSequence text) {
        membersListPresenter.onSearch(text);
    }


    @OnClick(R.id.et_topic_member_search)
    void onSearchInputClick() {
        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.SearchInputField);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        membersListPresenter.onDestroy();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_invitation:
                onInviteOptionSelect();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void onHomeOptionSelect() {
        finish();
    }

    void onInviteOptionSelect() {
        if (type == TYPE_MEMBERS_LIST_TEAM) {
            InviteDialogExecutor.getInstance().executeInvite(this);
        } else if (type == TYPE_MEMBERS_LIST_TOPIC) {
            membersListPresenter.inviteMemberToTopic(entityId);
        }
    }

    @OnClick(value = {
            R.id.img_chat_choose_member_empty,
            R.id.btn_chat_choose_member_empty})
    void onMemberJoinClick() {
        onInviteOptionSelect();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void onEventMainThread(final RequestMoveDirectMessageEvent event) {

        moveDirectMessageActivity(TeamInfoLoader.getInstance().getTeamId(), event.userId);
    }

    public void onEventMainThread(ShowProfileEvent event) {

        Completable.fromAction(() -> {
            moveToProfile(event.userId);
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    public void onEvent(TeamJoinEvent event) {
        membersListPresenter.onSearch(tvSearch.getText().toString());
    }

    public void onEvent(TeamLeaveEvent event) {
        membersListPresenter.onSearch(tvSearch.getText().toString());
    }

    void initProgressWheel() {
        mProgressWheel = new ProgressWheel(MembersListActivity.this);
    }

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

    @Override
    public void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

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

    @Override
    public void moveDirectMessageActivity(long teamId, long userId) {
        startActivity(Henson.with(MembersListActivity.this)
                .gotoMessageListV2Activity()
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .roomId(-1)
                .entityId(userId)
                .isFromPush(false)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void showInviteSucceed(int memberSize) {
        String rawString = getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);
        ColoredToast.show(formatString);
    }

    @Override
    public void showInviteFailed(String errMessage) {
        ColoredToast.showError(errMessage);
    }

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

    @Override
    public void showDialogKick(String userName, String userProfileUrl, long memberId) {
        KickDialogFragment dialogFragment = KickDialogFragment.create(userName, userProfileUrl);

        dialogFragment.setOnKickConfirmClickListener((dialog, which) -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Participants, AnalyticsValue.Action.KickMember);
            membersListPresenter.onKickUser(entityId, memberId);
        });

        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

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

    @Override
    public void refreshMemberList() {
        membersListPresenter.onSearch(tvSearch.getText());
    }

    @Override
    public void showKickSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_success_kick_user_from_topic));
    }

    @Override
    public void showKickFailToast() {
        ColoredToast.show(getString(R.string.jandi_err_unexpected));
    }

    @Override
    public void showAlreadyTopicOwnerToast() {
        ColoredToast.showError(getString(R.string.jandi_alert_already_topic_owenr));
    }

    @Override
    public void showNeedToAssignTopicOwnerDialog() {
        new AlertDialog.Builder(MembersListActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.jandi_topic_owner_title)
                .setMessage(R.string.jandi_guide_to_assign_topic_owner)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create().show();
    }

    @Override
    public void showConfirmAssignTopicOwnerDialog(String userName, String userProfileUrl, long memberId) {
        AssignTopicOwnerDialog assignDialog = AssignTopicOwnerDialog.create(userName, userProfileUrl);
        assignDialog.setConfirmListener((dialog, which) -> {
            membersListPresenter.onAssignToTopicOwner(entityId, memberId);
        });
        assignDialog.show(getSupportFragmentManager(), "assign_dialog");
    }

    @Override
    public void showAssignTopicOwnerSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_complete_assign_topic_owner));
    }

    @Override
    public void showAssignTopicOwnerFailToast() {
        ColoredToast.showError(getString(R.string.jandi_err_unexpected));
    }

    @Override
    public void setResultAndFinish(long memberId) {
        if (memberId > 0) {
            Intent intent = new Intent();
            intent.putExtra(KEY_MEMBER_ID, memberId);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void showDialogGuestKick(long memberId) {
        String title = getString(R.string.topic_remove_associatewithonetopic_title);
        String message = getString(R.string.topic_remove_associatewithonetopic_desc);
        new AlertDialog.Builder(MembersListActivity.this)
                .setMessage(Html.fromHtml(String.format("<b>%s</b><br/><br/>%s", title, message)))
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.topic_remove_associatewithonetopic_remove, (dialog, which) -> {
                    membersListPresenter.onKickUser(entityId, memberId);
                })
                .create()
                .show();
    }

    @Override
    public void moveToProfile(long userId) {
        if (AccessLevelUtil.hasAccessLevel(userId)) {
            startActivity(Henson.with(this)
                    .gotoMemberProfileActivity()
                    .memberId(userId)
                    .from(getType() == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC ?
                            MemberProfileActivity.EXTRA_FROM_PARTICIPANT : MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                    .build());
        } else {
            AccessLevelUtil.showDialogUnabledAccessLevel(this);
        }

        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.ViewProfile);
    }

    @Override
    public void inviteMember(long entityId) {
        InviteDialogExecutor.getInstance().executeInvite(this);
    }

}