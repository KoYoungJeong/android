package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.view;

import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicListView {

    void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId, long markerLinkId);

    void showTopicInfoDialog(Topic item);

    void notifyDataSetChanged();

    void showProgressWheel();

    void showToast(String message);

    void showErrorToast(String message);

    void dismissProgressWheel();

    void showHasNoTopicToJoinErrorToast();

    void finish();

    void showJoinToTopicErrorToast();

    void showJoinToTopicToast(String name);

    void showSelectedTopic(int position);

    void showEmptyQueryMessage(String query);

    void hideEmptyQueryMessage();
}
