package com.tosslab.jandi.app.ui.maintab.topic.create;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.maintab.topic.create.model.TopicCreateModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.TextChange;
import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EActivity(R.layout.activity_topic_create)
@OptionsMenu(R.menu.add_topic_text)
public class TopicCreateActivity extends Activity {

    private static final Logger logger = Logger.getLogger(TopicCreateActivity.class);

    @Bean
    TopicCreateModel topicCreateModel;

    @Bean
    TopicCreatePresenter topicCreatePresenter;

    @OptionsItem(R.id.action_add_topic)
    void onAddOptionClick() {
        String topicTitle = topicCreatePresenter.getTopicTitle();

        if (TextUtils.isEmpty(topicTitle)) {
            return;
        }

        boolean publicSelected = topicCreatePresenter.isPublicSelected();

        createTopic(topicTitle, publicSelected);
    }

    @TextChange(R.id.et_topic_create_title)
    void onTitleTextChange(TextView textView, CharSequence text) {

        if (topicCreateModel.isOverMaxLength(text)) {

            textView.setText(text.subSequence(0, TopicCreateModel.TITLE_MAX_LENGTH));
        }

    }

    @Background
    void createTopic(String topicTitle, boolean publicSelected) {
        topicCreatePresenter.showProgressWheel();
        try {
            ResCommon topic = topicCreateModel.createTopic(topicTitle, publicSelected);

            try {
                EntityManager mEntityManager = EntityManager.getInstance(TopicCreateActivity.this);
                if (mEntityManager != null) {
                    MixpanelMemberAnalyticsClient
                            .getInstance(TopicCreateActivity.this, mEntityManager.getDistictId())
                            .trackCreatingEntity(true);
                }
            } catch (JSONException e) {
                logger.error("CAN NOT MEET", e);
            }

            topicCreateModel.refreshEntity();

            EntityManager.getInstance(TopicCreateActivity.this).refreshEntity(TopicCreateActivity.this);

            topicCreatePresenter.createTopicSuccess(topic.id, topicTitle, publicSelected);

        } catch (JandiNetworkException e) {
            logger.error(e.getErrorInfo(), e);
            if (e.errCode == JandiNetworkException.DUPLICATED_NAME) {
                topicCreatePresenter.createTopicFailed(R.string.err_entity_duplicated_name);
            } else {
                topicCreatePresenter.createTopicFailed(R.string.err_entity_create);
            }
        } finally {
            topicCreatePresenter.dismissProgressWheel();
        }
    }

    @Click(R.id.layout_topic_create_public_check)
    void onPublicTypeClick() {
        topicCreatePresenter.setTopicType(true);
        topicCreatePresenter.setTopicTip(true);
    }


    @Click(R.id.layout_topic_create_private_check)
    void onPrivateTypeClick() {
        topicCreatePresenter.setTopicType(false);
        topicCreatePresenter.setTopicTip(false);

    }


}
