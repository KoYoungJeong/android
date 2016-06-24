package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestVotePollEvent;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class PollVoteViewHolder extends BaseViewHolder<Poll> {

    private TextView tvPollItemVote;

    private PollVoteViewHolder(View itemView) {
        super(itemView);
        tvPollItemVote = (TextView) itemView.findViewById(R.id.tv_poll_detail_item_vote);
    }

    public static PollVoteViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_item_vote, parent, false);
        return new PollVoteViewHolder(itemView);
    }

    @Override
    public void onBindView(Poll poll) {
        Collection<Integer> votedItemSeqs = poll.getVotedItemSeqs();

        itemView.setEnabled(votedItemSeqs != null && votedItemSeqs.size() > 0);

        String vote = "투표하기";
        if (poll.isAnonymous()) {
            vote += " (익명)";
        }
        tvPollItemVote.setText(vote);

        itemView.setOnClickListener(v ->
                EventBus.getDefault().post(RequestVotePollEvent.create(poll.getId(), votedItemSeqs)));
    }

}
