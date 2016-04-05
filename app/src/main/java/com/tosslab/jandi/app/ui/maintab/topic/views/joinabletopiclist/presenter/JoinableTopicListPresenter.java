package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.presenter;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicListPresenter {

    void initSearchTopicQueue();

    void stopSearchTopicQueue();

    void onInitJoinableTopics();

    void onJoinTopic(long topic);

    void onTopicClick(int position);

    void onSearchTopic(CharSequence query);

    void onShouldShowSelectedTopic(long topicEntityId);
}
