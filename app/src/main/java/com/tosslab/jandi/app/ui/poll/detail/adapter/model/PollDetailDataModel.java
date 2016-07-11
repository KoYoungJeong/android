package com.tosslab.jandi.app.ui.poll.detail.adapter.model;

import android.util.Pair;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPollComments;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;

import java.util.Collection;
import java.util.List;

/**
 * Created by tonyjs on 16. 6. 15..
 */
public interface PollDetailDataModel {
    void removePollDetailRow();

    boolean hasCommentRows();

    void setPollDetails(Poll poll);

    void replacePollDetails(Poll poll);

    void addPollComments(List<ResMessages.OriginalMessage> comments);

    void addPollComment(ResMessages.OriginalMessage comment);

    void addPollComment(int position, ResMessages.OriginalMessage comment);

    void removeAllRows();

    void modifyCommentStarredState(long messageId, boolean starred);

    Pair<Integer, ResMessages.OriginalMessage> removeCommentByMessageIdAndGet(long messageId);

    void removePollComment(long messageId);

    Poll getPoll();

    ResMessages.Link getPollCommentById(long id);
}
