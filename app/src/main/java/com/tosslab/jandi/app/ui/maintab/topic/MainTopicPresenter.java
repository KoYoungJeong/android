package com.tosslab.jandi.app.ui.maintab.topic;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.ExpandableListView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.ui.maintab.topic.adapter.TopicListAdapter;
import com.tosslab.jandi.app.ui.message.MessageListActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainTopicPresenter {

    private static final Logger logger = Logger.getLogger(MainTopicPresenter.class);

    @RootContext
    Context context;

    @ViewById(R.id.list_main_topic)
    ExpandableListView topicListView;

    TopicListAdapter topicListAdapter;

    @AfterInject
    void initObject() {
        topicListAdapter = new TopicListAdapter(context);
    }

    @AfterViews
    void initViews() {
        topicListView.setAdapter(topicListAdapter);
    }

    @UiThread
    public void createTopicSucceed(int entityId, String entityName) {
        String rawString = context.getString(R.string.jandi_message_create_entity);
        String formatString = String.format(rawString, entityName);
        ColoredToast.show(context, formatString);
        try {
            EntityManager entityManager = ((JandiApplication) context.getApplicationContext()).getEntityManager();
            if (entityManager != null) {
                MixpanelMemberAnalyticsClient
                        .getInstance(context, entityManager.getDistictId())
                        .trackCreatingEntity(true);
            }
        } catch (JSONException e) {
            logger.error("CAN NOT MEET", e);
        }
        moveToPublicTopicMessageActivity(entityId);
    }

    @UiThread
    public void createTopicFailed(int errStringResId) {
        ColoredToast.showError(context, context.getString(errStringResId));
    }

    private void moveToPublicTopicMessageActivity(int channelId) {
        ((JandiApplication) context.getApplicationContext()).setEntityManager(null);
        moveToMessageActivity(channelId, JandiConstants.TYPE_PUBLIC_TOPIC, false);
    }

    private void moveToMessageActivity(final int entityId, final int entityType, final boolean isStarred) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageListActivity_.intent(context)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .entityType(entityType)
                        .entityId(entityId)
                        .isFavorite(isStarred)
                        .start();
            }
        }, 250);
    }

    public void setEntities(List<FormattedEntity> joinEntities, List<FormattedEntity> unjoinEntities) {

        topicListAdapter
                .joinEntities(joinEntities)
                .unjoinEntities(unjoinEntities)
                .notifyDataSetChanged();

    }

}
