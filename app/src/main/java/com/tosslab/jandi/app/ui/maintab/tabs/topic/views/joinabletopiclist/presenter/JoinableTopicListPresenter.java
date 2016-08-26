package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.presenter;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicListPresenter {

    void initSearchTopicQueue();

    void stopSearchTopicQueue();

    void onJoinTopic(long topic);

    void onTopicClick(int position);

    void onSearchTopic(boolean withProgress, CharSequence query);

    void onShouldShowSelectedTopic(long topicEntityId);
}
