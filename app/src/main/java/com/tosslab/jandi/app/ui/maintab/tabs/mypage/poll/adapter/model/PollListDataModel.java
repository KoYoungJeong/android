package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.model;

import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public interface PollListDataModel {

    void addPolls(List<Poll> polls);

    void addPoll(int position, Poll poll);

    int getIndexOfFirstFinishedPoll();

    int getIndexById(long pollId);

    void removePollByIndex(int index);

    void removePollByIdAndStats(long pollId, String status);

    void setPoll(int index, Poll poll);
}
