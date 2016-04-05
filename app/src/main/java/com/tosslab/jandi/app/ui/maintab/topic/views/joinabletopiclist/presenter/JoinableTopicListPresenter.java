package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.presenter;

import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicListPresenter {

    void initSearchTopicQueue();

    void stopSearchTopicQueue();

    void onInitJoinableTopics();

    void onJoinTopic(Topic topic);

    void onTopicClick(int position);

    void onTopicLongClick(int position);

    void onSearchTopic(CharSequence query);
}
