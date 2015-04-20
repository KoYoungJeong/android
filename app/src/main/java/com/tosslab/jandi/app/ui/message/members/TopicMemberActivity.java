package com.tosslab.jandi.app.ui.message.members;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.message.members.model.TopicMemberModel;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EActivity(R.layout.activity_topic_member)
public class TopicMemberActivity extends ActionBarActivity {

    @Bean
    TopicMemberPresenter topicMemberPresenter;

    @Bean
    TopicMemberModel topicMemberModel;

    @Extra
    int entityId;


    @AfterViews
    void initViews() {
        getTopicMembers();

        setupActionbar();
    }

    private void setupActionbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }

    @Background
    void getTopicMembers() {
        List<ChatChooseItem> topicMembers = topicMemberModel.getTopicMembers(entityId);

        topicMemberPresenter.setTopicMembers(topicMembers);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(ProfileDetailEvent event) {
        int entityId = event.getEntityId();
        UserInfoDialogFragment_.builder().entityId(entityId).build().show(getSupportFragmentManager(), "dialog");
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        EntityManager entityManager = EntityManager.getInstance(TopicMemberActivity.this);
        MessageListV2Activity_.intent(TopicMemberActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .roomId(-1)
                .entityId(event.userId)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .isFromPush(false)
                .start();
    }
}
