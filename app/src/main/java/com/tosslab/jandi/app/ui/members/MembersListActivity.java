package com.tosslab.jandi.app.ui.members;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.members.adapter.MembersAdapter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
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
public class MembersListActivity extends AppCompatActivity implements MembersListPresenter.View {

    public static final int TYPE_MEMBERS_LIST_TEAM = 0x01;
    public static final int TYPE_MEMBERS_LIST_TOPIC = 0x02;

    @Bean(MembersListPresenterImpl.class)
    MembersListPresenter membersListPresenter;

    @ViewById(R.id.list_topic_member)
    RecyclerView memberListView;

    @ViewById(R.id.vg_topic_member_search_bar)
    View vgSearchbar;

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
    void initViews() {

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

    @UiThread
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