package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.viewholder;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public class PollViewHolder extends BaseViewHolder<Poll> {

    @Bind(R.id.tv_poll_list_subject)
    TextView tvSubject;
    @Bind(R.id.tv_poll_list_duedate)
    TextView tvDueDate;
    @Bind(R.id.tv_poll_list_status_badge)
    TextView tvStatusBadge;
    @Bind(R.id.tv_poll_list_finished_badge)
    TextView tvFinishedBadge;
    @Bind(R.id.tv_poll_list_creator)
    TextView tvCreator;
    @Bind(R.id.v_poll_list_creator_line_through)
    View vCreatorCovoer;
    @Bind(R.id.tv_poll_list_create_date)
    TextView tvCreateDate;
    @Bind(R.id.v_poll_list_icon)
    ImageView vPollIcon;
    @Bind(R.id.vg_poll_list_info)
    View vgPollListInfo;


    private PollViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static PollViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_list, parent, false);
        return new PollViewHolder(itemView);
    }

    @Override
    public void onBindView(Poll poll) {

        bindCreator(poll);

        bindPoll(poll);

    }

    private void bindPoll(Poll poll) {
        tvSubject.setText(poll.getSubject());
        tvCreateDate.setText(DateTransformator.getTimeString(poll.getCreatedAt()));

        String status = poll.getStatus();

        if ("created".equals(status)) {
            vPollIcon.setImageResource(R.drawable.poll_icon_normal_192);
            String dueDate = DateTransformator.getRemainingDays(poll.getDueDate());
            tvDueDate.setText(dueDate);

            tvStatusBadge.setVisibility("voted".equals(poll.getVoteStatus())
                    ? View.VISIBLE : View.GONE);

            tvFinishedBadge.setVisibility(View.GONE);

            tvCreator.setTextColor(0xff333333);
            tvSubject.setTextColor(0xff333333);
            tvDueDate.setTextColor(0xff999999);
            tvCreateDate.setTextColor(0xff999999);
        } else {
            vPollIcon.setImageResource(R.drawable.poll_icon_closed_192);

            String finished = DateTransformator.getTimeString(poll.getFinishedAt());
            tvDueDate.setText(finished);

            tvStatusBadge.setVisibility(View.GONE);
            tvFinishedBadge.setVisibility(View.VISIBLE);

            tvCreator.setTextColor(0xffcccccc);
            tvSubject.setTextColor(0xffcccccc);
            tvDueDate.setTextColor(0xffcccccc);
            tvCreateDate.setTextColor(0xffcccccc);
        }
    }

    void bindCreator(Poll poll) {
        long creatorId = poll.getCreatorId();
        if (TeamInfoLoader.getInstance().isUser(creatorId)) {
            tvCreator.setText(TeamInfoLoader.getInstance().getMemberName(creatorId));

            Resources resources = tvCreator.getResources();
            if (TeamInfoLoader.getInstance().getUser(creatorId).isEnabled()) {
                tvCreator.setTextColor(0xff333333);
                vCreatorCovoer.setVisibility(View.GONE);
            } else {
                tvCreator.setTextColor(0xff999999);
                vCreatorCovoer.setVisibility(View.VISIBLE);
            }
        }
    }

}
