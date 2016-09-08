package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter;

/**
 * Created by jsuch2362 on 15. 11. 19..
 */
public interface TopicCreatePresenter {

    void onCreateTopic(String topicTitle, String topicDescriptionText, boolean isPublic, boolean isAutojoin);

    void setView(View view);

    interface View {

        void showCheckNetworkDialog();

        void showProgressWheel();

        void dismissProgressWheel();

        void createTopicSuccess(long teamId, long id, String topicTitle, boolean isPublic);

        void createTopicFailed(int err_entity_duplicated_name);
    }
}
