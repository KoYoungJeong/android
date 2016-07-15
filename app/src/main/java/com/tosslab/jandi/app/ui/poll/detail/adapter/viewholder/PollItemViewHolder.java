package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.content.res.Resources;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.poll.PollDataChangedEvent;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.viewgroup.ProgressRelativeLayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class PollItemViewHolder extends BaseViewHolder<Pair<Poll, Poll.Item>> {

    private ProgressRelativeLayout progressBar;
    private View vSelectedIcon;
    private View vSpace;
    private TextView tvTitle;
    private TextView tvParticipants;
    private View vParticipants;
    private int defaultTitleLeftMargin;

    private OnPollItemParticipantsClickListener onPollItemParticipantsClickListener;

    private PollItemViewHolder(View itemView,
                               OnPollItemParticipantsClickListener onPollItemParticipantsClickListener) {
        super(itemView);
        this.onPollItemParticipantsClickListener = onPollItemParticipantsClickListener;

        progressBar = (ProgressRelativeLayout) itemView.findViewById(R.id.progress_bar);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_poll_detail_item_title);
        tvParticipants = (TextView) itemView.findViewById(R.id.tv_poll_detail_item_participants);
        vParticipants = itemView.findViewById(R.id.vg_poll_detail_item_participants);
        vSelectedIcon = itemView.findViewById(R.id.v_poll_detail_item_selected);
        vSpace = itemView.findViewById(R.id.space_poll_detail_item);

        defaultTitleLeftMargin = (int) UiUtils.getPixelFromDp(15);
    }

    public static PollItemViewHolder newInstance(ViewGroup parent,
                                                 OnPollItemParticipantsClickListener onPollItemParticipantsClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_item, parent, false);
        return new PollItemViewHolder(itemView, onPollItemParticipantsClickListener);
    }

    @Override
    public void onBindView(Pair<Poll, Poll.Item> pollItemPair) {

        Poll poll = pollItemPair.first;
        Poll.Item item = pollItemPair.second;

        tvTitle.setText(item.getName());

        if ("enabled".equals(poll.getVoteStatus())) {

            bindSelectablePoll(poll, item);

        } else {

            bindUnSelectablePoll(poll, item);

        }

    }

    void bindSelectablePoll(final Poll poll, final Poll.Item item) {
        vParticipants.setVisibility(View.INVISIBLE);

        progressBar.setProgress(0);
        progressBar.setMax(0);

        boolean isSelected = isInSelectedItems(poll.getVotedItemSeqs(), item.getSeq());

        Resources resources = itemView.getResources();
        int color = isSelected ? resources.getColor(R.color.jandi_horizontal_progress_selected)
                : resources.getColor(R.color.jandi_horizontal_progress_background);
        progressBar.setProgressBackgroundColor(color);

        vSelectedIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        vSpace.setVisibility(isSelected ? View.GONE : View.VISIBLE);

        final ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) tvTitle.getLayoutParams();
        marginLayoutParams.leftMargin = isSelected ? 0 : defaultTitleLeftMargin;
        tvTitle.setLayoutParams(marginLayoutParams);

        progressBar.setSelected(isSelected);

        progressBar.setOnClickListener(v -> selectItem(poll, item, !v.isSelected()));
    }

    private void bindUnSelectablePoll(final Poll poll, final Poll.Item item) {
        tvParticipants.setText(Integer.toString(item.getVotedCount()));
        vParticipants.setVisibility(View.VISIBLE);

        progressBar.setSelected(false);
        progressBar.setOnClickListener(v ->
                onPollItemParticipantsClickListener.onPollItemParticipantsClick(poll, item));

        progressBar.setProgress(item.getVotedCount());
        progressBar.setMax(poll.getVotedCount());

        Resources resources = itemView.getResources();
        int progressBackgroundColor =
                resources.getColor(R.color.jandi_horizontal_progress_background);
        progressBar.setProgressBackgroundColor(progressBackgroundColor);

        if ("created".equals(poll.getStatus())) {
            boolean amIVoted = "voted".equals(poll.getVoteStatus());

            int progressColor = amIVoted ? resources.getColor(R.color.jandi_horizontal_progress_voted)
                    : resources.getColor(R.color.jandi_horizontal_progress_selected);
            progressBar.setProgressColor(progressColor);
        } else {
            boolean isElectedItem = isElectedItem(poll.getElectedItems(), item.getSeq());

            int progressColor = isElectedItem ? resources.getColor(R.color.jandi_horizontal_progress_voted)
                    : resources.getColor(R.color.jandi_horizontal_progress_selected);
            progressBar.setProgressColor(progressColor);
        }

        boolean isSelected = isInSelectedItems(poll.getVotedItemSeqs(), item.getSeq());
        vSelectedIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        vSpace.setVisibility(isSelected ? View.GONE : View.VISIBLE);

        final ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) tvTitle.getLayoutParams();
        marginLayoutParams.leftMargin = isSelected ? 0 : defaultTitleLeftMargin;
        tvTitle.setLayoutParams(marginLayoutParams);
    }

    private boolean isElectedItem(Collection<Poll.Item> electedItems, int seq) {
        if (electedItems == null || electedItems.isEmpty()) {
            return false;
        }

        Iterator<Poll.Item> iterator = electedItems.iterator();
        while (iterator.hasNext()) {
            Poll.Item next = iterator.next();
            if (next.getSeq() == seq) {
                return true;
            }
        }

        return false;
    }

    private boolean isInSelectedItems(Collection<Integer> votedItemSeqs, int seq) {
        if (votedItemSeqs == null || votedItemSeqs.isEmpty()) {
            return false;
        }

        for (Integer item : votedItemSeqs) {
            if (item == seq) {
                return true;
            }
        }

        return false;
    }

    private void selectItem(Poll poll, Poll.Item item, boolean select) {
        Collection<Integer> votedItemSeqs = poll.getVotedItemSeqs();
        if (votedItemSeqs == null) {
            votedItemSeqs = new LinkedHashSet<>();
        }

        if (select) {
            if (!poll.isMultipleChoice()) {
                votedItemSeqs.clear();
            }

            votedItemSeqs.add(item.getSeq());

        } else {
            votedItemSeqs.remove(item.getSeq());
        }

        poll.setVotedItemSeqs(votedItemSeqs);

        EventBus.getDefault().post(PollDataChangedEvent.create(poll));
    }

    public interface OnPollItemParticipantsClickListener {
        void onPollItemParticipantsClick(Poll poll, Poll.Item item);
    }
}
