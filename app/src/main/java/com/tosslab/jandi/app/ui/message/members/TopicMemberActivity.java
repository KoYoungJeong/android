package com.tosslab.jandi.app.ui.message.members;

import android.app.Activity;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.message.members.model.TopicMemberModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EActivity(R.layout.activity_topic_member)
public class TopicMemberActivity extends Activity {

    @Bean
    TopicMemberPresenter topicMemberPresenter;

    @Bean
    TopicMemberModel topicMemberModel;

    @Extra
    int entityId;


    @AfterViews
    void initViews() {
        getTopicMembers();
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
        FormattedEntity entity = EntityManager.getInstance(TopicMemberActivity.this).getEntityById(entityId);
        UserInfoDialogFragment.newInstance(entity, false).show(getFragmentManager(), "dialog");
    }
}
