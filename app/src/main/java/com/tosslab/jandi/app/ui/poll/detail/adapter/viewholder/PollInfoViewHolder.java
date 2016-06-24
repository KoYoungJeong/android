package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ProfileBinder;
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

    private PollInfoViewHolder(View itemView) {
        super(itemView);
        tvSubject = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_subject);
        tvDueDate = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_duedate);
        tvParticipants = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_participants_count);
        tvOptions = (TextView) itemView.findViewById(R.id.tv_poll_detail_info_options);
        btnParticipants = itemView.findViewById(R.id.btn_poll_detail_info_participants);
    }

    public static PollInfoViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_info, parent, false);
        return new PollInfoViewHolder(itemView);
    }

    @Override
    public void onBindView(Poll poll) {

        tvSubject.setText(poll.getSubject());
        tvParticipants.setText(poll.getVotedCount() + "명 참여");

        if (hasDueDate(poll)) {
            tvDueDate.setSelected(true);
            btnParticipants.setSelected(true);
            btnParticipants.setOnClickListener(v -> {

            });

            String leftDays = DateTransformator.getLeftDays(poll.getDueDate());
            tvDueDate.setText(leftDays);
        } else {
            tvDueDate.setSelected(false);
            btnParticipants.setSelected(false);
            btnParticipants.setOnClickListener(v -> {

            });

            tvDueDate.setText(DateTransformator.getTimeString(poll.getDueDate()) + " 종료됨");
        }

        if (!poll.isAnonymous() && !poll.isMultipleChoice()) {
            tvOptions.setVisibility(View.GONE);
        } else {
            tvOptions.setVisibility(View.VISIBLE);

            StringBuffer sb = new StringBuffer();
            if (poll.isAnonymous()) {
                sb.append("익명투표");

                if (poll.isMultipleChoice()) {
                    sb.append(", 중복투표 가능");
                }

            } else {
                sb.append("중복투표 가능");
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
}
