package com.tosslab.jandi.app.ui.poll.list.adapter.model;

import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public interface PollListDataModel {

    void addPolls(List<Poll> polls);

    void addPoll(Poll poll);

    void addPoll(int position, Poll poll);

    int getIndexOfFirstFinishedPoll();

    int getIndexById(long pollId);

    void removePollByIndex(int index);

    void removePollByIdAndStats(long pollId, String status);
}
