package com.tosslab.jandi.app.ui.maintab.topic.create;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.maintab.topic.create.model.TopicCreateModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.json.JSONException;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EActivity(R.layout.activity_topic_create)
@OptionsMenu(R.menu.add_topic_text)
public class TopicCreateActivity extends AppCompatActivity {

    @Bean
    TopicCreateModel topicCreateModel;

    @Bean
    TopicCreatePresenter topicCreatePresenter;

    @OptionsMenuItem(R.id.action_add_topic)
    MenuItem menuCreatTopic;

    @AfterViews
    void initViews() {
        setupActionBar();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @OptionsItem(R.id.action_add_topic)
    void onAddOptionClick() {
        String topicTitle = topicCreatePresenter.getTopicTitle();

        if (TextUtils.isEmpty(topicTitle)) {
            return;
        }

        boolean publicSelected = topicCreatePresenter.isPublicSelected();

        createTopic(topicTitle, publicSelected);
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionClick() {
        finish();
    }

    @TextChange(R.id.et_topic_create_title)
    void onTitleTextChange(TextView textView, CharSequence text) {

        if (topicCreateModel.isOverMaxLength(text)) {

            textView.setText(text.subSequence(0, TopicCreateModel.TITLE_MAX_LENGTH));
        }

        if (TextUtils.isEmpty(textView.getText())) {
            menuCreatTopic.setEnabled(false);
        } else {
            menuCreatTopic.setEnabled(true);
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
                LogUtil.e("CAN NOT MEET", e);
            }

            topicCreateModel.refreshEntity();

            topicCreatePresenter.dismissProgressWheel();

            EntityManager.getInstance(TopicCreateActivity.this).refreshEntity(TopicCreateActivity.this);
            int teamId = JandiAccountDatabaseManager.getInstance(TopicCreateActivity.this).getSelectedTeamInfo().getTeamId();
            topicCreatePresenter.createTopicSuccess(teamId, topic.id, topicTitle, publicSelected);
        } catch (RetrofitError e) {
            topicCreatePresenter.dismissProgressWheel();
            if (e.getResponse() != null && e.getResponse().getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                topicCreatePresenter.createTopicFailed(R.string.err_entity_duplicated_name);
            } else {
                topicCreatePresenter.createTopicFailed(R.string.err_entity_create);
            }
        } catch (Exception e) {
            topicCreatePresenter.dismissProgressWheel();
            topicCreatePresenter.createTopicFailed(R.string.err_entity_create);
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
