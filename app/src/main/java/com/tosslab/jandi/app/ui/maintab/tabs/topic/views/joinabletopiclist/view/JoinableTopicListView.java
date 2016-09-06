package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view;

import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicListView {

    void moveToMessageActivity(long entityId, int entityType, boolean starred, long teamId, long markerLinkId);

    void showTopicInfoDialog(final TopicRoom item);

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
