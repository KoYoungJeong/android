package com.tosslab.jandi.app.ui.poll.util;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollBinder {

    public static void bindPoll(Poll poll,
                                boolean dueDateToRemainingTime,
                                ImageView vPollIcon,
                                TextView tvSubject, TextView tvDescription, TextView tvCreator,
                                TextView tvDueDate, TextView tvPollDeleted) {
        if (poll == null) {
            return;
        }

        tvSubject.setText(poll.getSubject());

        if (poll.getDescription() != null && !TextUtils.isEmpty(poll.getDescription())) {
            tvDescription.setText(poll.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
        tvCreator.setText(TeamInfoLoader.getInstance().getMemberName(poll.getCreatorId()));

        String status = poll.getStatus();

        if ("created".equals(status)) {
            if (TextUtils.equals(poll.getVoteStatus(), "voted")) {
                tvDueDate.setText(R.string.jandi_poll_voted);
            } else {
                String dueDate = dueDateToRemainingTime
                        ? DateTransformator.getRemainingDays(poll.getDueDate())
                        : tvDueDate.getContext().getString(R.string.jandi_poll_till,
                        DateTransformator.getTimeString(poll.getDueDate()));
                tvDueDate.setText(dueDate);
            }
            vPollIcon.setImageResource(R.drawable.poll_icon_normal_135);
        } else {
            if (TextUtils.equals(status, "deleted")) {
                vPollIcon.setImageResource(R.drawable.poll_icon_deleted_135);
            } else {
                vPollIcon.setImageResource(R.drawable.poll_icon_closed_135);
            }
            tvDueDate.setText(R.string.jandi_finished);
        }

        boolean deleted = "deleted".equals(status);
        tvSubject.setVisibility(deleted ? View.GONE : View.VISIBLE);
        if (deleted) {
            tvDescription.setVisibility(View.GONE);
        }
        tvCreator.setVisibility(deleted ? View.GONE : View.VISIBLE);
        tvDueDate.setVisibility(deleted ? View.GONE : View.VISIBLE);
        tvPollDeleted.setVisibility(deleted ? View.VISIBLE : View.GONE);
    }

    public static void bindPoll(Poll poll,
                                boolean dueDateToRemainingTime,
                                ImageView vPollIcon,
                                TextView tvSubject, TextView tvCreator,
                                TextView tvDueDate, TextView tvPollDeleted) {
        if (poll == null) {
            return;
        }

        tvSubject.setText(poll.getSubject());
        tvCreator.setText(TeamInfoLoader.getInstance().getMemberName(poll.getCreatorId()));

        String status = poll.getStatus();

        if ("created".equals(status)) {
            if (!poll.getVoteStatus().equals("voted")) {
                String dueDate = dueDateToRemainingTime
                        ? DateTransformator.getRemainingDays(poll.getDueDate())
                        : tvDueDate.getContext().getString(R.string.jandi_poll_till,
                        DateTransformator.getTimeString(poll.getDueDate()));
                tvDueDate.setText(dueDate);

            } else {
                tvDueDate.setText(R.string.jandi_poll_voted);
            }
            vPollIcon.setImageResource(R.drawable.poll_icon_normal_135);
        } else {
            if (TextUtils.equals(status, "deleted")) {
                vPollIcon.setImageResource(R.drawable.poll_icon_deleted_135);
            } else {
                vPollIcon.setImageResource(R.drawable.poll_icon_closed_135);
            }
            tvDueDate.setText(R.string.jandi_finished);
        }

        boolean deleted = "deleted".equals(status);
        tvSubject.setVisibility(deleted ? View.GONE : View.VISIBLE);
        tvCreator.setVisibility(deleted ? View.GONE : View.VISIBLE);
        tvDueDate.setVisibility(deleted ? View.GONE : View.VISIBLE);
        tvPollDeleted.setVisibility(deleted ? View.VISIBLE : View.GONE);
    }
}
