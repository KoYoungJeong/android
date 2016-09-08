package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.model;

import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;

import java.util.List;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public interface JoinableTopicDataModel {
    void setJoinableTopics(List<Topic> topics);

    void clear();

    Topic getItem(int position);

    Topic getItemByEntityId(long entityId);

    int getPositionByTopicEntityId(long entityId);
}
