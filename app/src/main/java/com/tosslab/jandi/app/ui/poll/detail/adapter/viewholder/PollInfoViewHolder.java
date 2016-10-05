package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

import java.util.Date;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class PollInfoViewHolder extends BaseViewHolder<Poll> {

    private TextView tvSubject;
    private TextView tvDueDate;
    private TextView tvParticipants;
    private TextView tvOptions;

    private View btnParticipants;
    private View vParticipantsMetaphor;

    private OnPollParticipantsClickListener onPollParticipantsClickListener;

    private PollInfoViewHolder(View itemView, OnPollParticipantsClickListener onPollParticipantsClickListener) {
        super(itemView);
        this.onPollParticipantsClickListener = onPollParticipantsClickListener;

        tvSubject = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_subject);
        tvDueDate = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_duedate);
        tvParticipants = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_participants_count);
        tvOptions = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_options);
        btnParticipants = itemView.findViewById(R.id.btn_poll_detail_info_participants);
        vParticipantsMetaphor = itemView.findViewById(R.id.v_poll_detail_info_participants_arrow);
    }

    public static PollInfoViewHolder newInstance(ViewGroup parent,
                                                 OnPollParticipantsClickListener onPollParticipantsClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_info, parent, false);
        return new PollInfoViewHolder(itemView, onPollParticipantsClickListener);
    }

    @Override
    public void onBindView(Poll poll) {
        Resources resources = tvSubject.getResources();

        tvSubject.setText(poll.getSubject());
        String participants =
                poll.getVotedCount() + resources.getString(R.string.jandi_participants);
        tvParticipants.setText(participants);

        if (hasDueDate(poll)) {
            tvDueDate.setSelected(false);
            String leftDays = DateTransformator.getRemainingDays(poll.getDueDate());
            tvDueDate.setText(leftDays);

            boolean canShowParticipants = !poll.isAnonymous() && "voted".equals(poll.getVoteStatus());
            btnParticipants.setSelected(canShowParticipants);
            vParticipantsMetaphor.setVisibility(canShowParticipants ? View.VISIBLE : View.GONE);
            if (canShowParticipants) {
                btnParticipants.setOnClickListener(v ->
                        onPollParticipantsClickListener.onAllParticipantsClick(poll));
            } else {
                btnParticipants.setOnClickListener(null);
            }
        } else {
            tvDueDate.setSelected(true);
            btnParticipants.setSelected(true);
            vParticipantsMetaphor.setVisibility(View.VISIBLE);
            btnParticipants.setOnClickListener(v ->
                    onPollParticipantsClickListener.onAllParticipantsClick(poll));
            String finished = DateTransformator.getTimeString(poll.getFinishedAt()) +
                    " " + resources.getString(R.string.jandi_finished);
            tvDueDate.setText(finished);
        }

        if (!poll.isAnonymous() && !poll.isMultipleChoice()) {
            tvOptions.setVisibility(View.GONE);
        } else {
            tvOptions.setVisibility(View.VISIBLE);
            StringBuffer sb = new StringBuffer();
            if (poll.isAnonymous()) {
                sb.append(resources.getString(R.string.jandi_poll_anonymous));

                if (poll.isMultipleChoice()) {
                    sb.append(", " + resources.getString(R.string.jandi_poll_multiple));
                }

            } else {
                sb.append(resources.getString(R.string.jandi_poll_multiple));
            }

            tvOptions.setText(sb.toString());
        }

    }

    private boolean hasDueDate(Poll poll) {
        if ("created".equals(poll.getStatus())) {
            Date now = new Date();
            int compare = now.compareTo(poll.getDueDate());
            if (compare <= 0) {
                return true;
            }
        }
        return false;
    }

    public interface OnPollParticipantsClickListener {
        void onAllParticipantsClick(Poll poll);
    }

}
