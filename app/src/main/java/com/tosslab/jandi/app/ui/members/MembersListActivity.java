package com.tosslab.jandi.app.ui.members;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.members.adapter.MembersAdapter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by tee on 15. 6. 3..
 */

@EActivity(R.layout.activity_topic_member)
public class MembersListActivity extends AppCompatActivity implements MembersListPresenter.View {

    public static final int TYPE_MEMBERS_LIST_TEAM = 0x01;
    public static final int TYPE_MEMBERS_LIST_TOPIC = 0x02;

    @Bean(MembersListPresenterImpl.class)
    MembersListPresenter membersListPresenter;

    @ViewById(R.id.list_topic_member)
    ListView memberListView;

    @Extra
    int entityId;

    @Extra
    int type;

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;

    private ProgressWheel mProgressWheel;

    private MembersAdapter topicMembersAdapter;

    @AfterInject
    void initObject() {
        topicMembersAdapter = new MembersAdapter(getApplicationContext());
        membersListPresenter.setView(this);
    }

    @AfterViews
    void initViews() {        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.TEAM_MEMBER)
                        .build());
        memberListView.setAdapter(topicMembersAdapter);
        initProgressWheel();
    }

    @AfterViews
    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        if (type == TYPE_MEMBERS_LIST_TEAM) {
            actionBar.setTitle(R.string.jandi_team_member);
        } else {
            actionBar.setTitle(R.string.jandi_topic_paricipants);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_info_menu, menu);
        if (type != TYPE_MEMBERS_LIST_TEAM) {
            menu.findItem(R.id.action_invitation).setVisible(false);
        }
        return true;
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
        // Progress Wheel 설정
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
        //FIXME
    void onInviteOptionSelect() {
        invitationDialogExecutor.execute();
    }

    @Override
    public void showListMembers(List<ChatChooseItem> topicMembers) {
        topicMembersAdapter.clear();
        topicMembersAdapter.addAll(topicMembers);
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
}