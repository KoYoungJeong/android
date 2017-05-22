package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import java.util.Collection;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class PollVoteViewHolder extends BaseViewHolder<Poll> {

    private TextView tvPollItemVote;
    private OnPollVoteClickListener onPollVoteClickListener;

    private PollVoteViewHolder(View itemView, OnPollVoteClickListener onPollVoteClickListener) {
        super(itemView);
        this.onPollVoteClickListener = onPollVoteClickListener;
        tvPollItemVote = (TextView) itemView.findViewById(R.id.tv_poll_detail_item_vote);
    }

    public static PollVoteViewHolder newInstance(ViewGroup parent,
                                                 OnPollVoteClickListener onPollVoteClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_item_vote, parent, false);
        return new PollVoteViewHolder(itemView, onPollVoteClickListener);
    }

    @Override
    public void onBindView(Poll poll) {
        Collection<Integer> votedItemSeqs = poll.getVotedItemSeqs();

        itemView.setEnabled(votedItemSeqs != null && votedItemSeqs.size() > 0);

        Resources resources = itemView.getResources();

        String vote;

        if (poll.isAnonymous() && !poll.isMultipleChoice()) {
            vote = resources.getString(R.string.jandi_vote_anonymous);
        } else if (poll.isMultipleChoice() && !poll.isAnonymous()) {
            vote = resources.getString(R.string.jandi_poll_voting_available_multi_choice);
        } else if (poll.isMultipleChoice() && poll.isAnonymous()) {
            vote = resources.getString(R.string.jandi_poll_voting_anonymous_available_multi_choice);
        } else {
            vote = resources.getString(R.string.jandi_vote);
        }

        tvPollItemVote.setText(vote);

        itemView.setOnClickListener(v ->
                onPollVoteClickListener.onPollVote(poll.getId(), votedItemSeqs));
    }

    public interface OnPollVoteClickListener {
        void onPollVote(long pollId, Collection<Integer> votedItemSeqs);
    }

}
