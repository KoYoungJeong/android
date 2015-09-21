package com.tosslab.jandi.app.ui.members;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
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
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.members.adapter.MembersAdapter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
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

/**
 * Created by tee on 15. 6. 3..
 */

@EActivity(R.layout.activity_topic_member)
public class MembersListActivity extends BaseAppCompatActivity implements MembersListPresenter.View {

    public static final int TYPE_MEMBERS_LIST_TEAM = 1;
    public static final int TYPE_MEMBERS_LIST_TOPIC = 2;
    public static final int TYPE_MEMBERS_JOINABLE_TOPIC = 3;

    @Extra
    int entityId;

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

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;

    private ProgressWheel mProgressWheel;

    private MembersAdapter topicMembersAdapter;

    @AfterInject
    void initObject() {
        topicMembersAdapter = new MembersAdapter(getApplicationContext());
        if (type == TYPE_MEMBERS_JOINABLE_TOPIC) {
            topicMembersAdapter.setEnableCheckMode();
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
        memberListView.setAdapter(topicMembersAdapter);
        initProgressWheel();

        int scropMaxY = getActionbarHeight();

        if (scropMaxY > 0) {
            memberListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    final int offset = (int) (dy * .66f);


                    final float futureScropViewPosY = vgSearchbar.getY() - offset;

                    if (futureScropViewPosY <= 0) {
                        vgSearchbar.setY(0);
                    } else if (futureScropViewPosY >= scropMaxY) {
                        vgSearchbar.setY(scropMaxY);
                    } else {
                        vgSearchbar.setY(futureScropViewPosY);
                    }
                }
            });
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
        membersListPresenter.onDestory();
    }

    private int getActionbarHeight() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);

        return TypedValue.complexToDimensionPixelOffset(typedValue.data, getResources()
                .getDisplayMetrics());

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

        if (type == TYPE_MEMBERS_LIST_TOPIC) {
            MenuItem menuItem = menu.findItem(R.id.action_invitation);
            menuItem.setVisible(false);
        } else if (type == TYPE_MEMBERS_JOINABLE_TOPIC) {
            MenuItem menuItem = menu.findItem(R.id.action_invitation);
            menuItem.setIcon(R.drawable.icon_actionbar_check);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    List<Integer> selectedCdp = topicMembersAdapter.getSelectedUserIds();

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

    @UiThread
    void showProgressWheel() {
        if (mProgressWheel == null) {
            mProgressWheel = new ProgressWheel(MembersListActivity.this);
        }

        if (!mProgressWheel.isShowing()) {
            mProgressWheel.show();
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

    @UiThread
    @Override
    public void showListMembers(List<ChatChooseItem> members) {
        topicMembersAdapter.clear();
        topicMembersAdapter.addAll(members);
        topicMembersAdapter.notifyDataSetChanged();
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public int getType() {
        return type;
    }

    @UiThread
    @Override
    public void moveDirectMessageActivity(int teamId, int userId, boolean isStarred) {
        MessageListV2Activity_.intent(MembersListActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .teamId(teamId)
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .roomId(-1)
                .entityId(userId)
                .isFavorite(isStarred)
                .isFromPush(false)
                .startForResult(MainTabActivity.REQ_START_MESSAGE);
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
        ColoredToast.show(this, formatString);
    }

    @Override
    @UiThread
    public void showInviteFailed(String errMessage) {
        ColoredToast.showError(this, errMessage);
    }

}