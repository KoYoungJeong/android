package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import android.text.TextUtils;
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

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollHeaderViewHolder extends BaseViewHolder<Poll> {

    private TextView tvUserName;
    private ImageView ivUserProfile;
    private View vUserNameDisableIndicator;
    private View vUserProfileDisableIndicator;
    private TextView tvCreatedDate;
    private View btnStar;
    private OnPollStarClickListener onPollStarClickListener;
    private ViewGroup vgProfileAbsence;

    private PollHeaderViewHolder(View itemView, OnPollStarClickListener onPollStarClickListener) {
        super(itemView);
        tvUserName = (TextView) itemView.findViewById(R.id.tv_poll_detail_user_name);
        ivUserProfile = (ImageView) itemView.findViewById(R.id.iv_poll_detail_user_profile);
        vgProfileAbsence = (ViewGroup) itemView.findViewById(R.id.vg_profile_absence);
        tvCreatedDate = (TextView) itemView.findViewById(R.id.tv_poll_detail_create_date);
        btnStar = itemView.findViewById(R.id.btn_poll_detail_star);

        vUserNameDisableIndicator =
                itemView.findViewById(R.id.v_poll_detail_user_name_disable_indicator);
        vUserProfileDisableIndicator =
                itemView.findViewById(R.id.v_poll_detail_user_profile_disable_indicator);
        this.onPollStarClickListener = onPollStarClickListener;
    }

    public static PollHeaderViewHolder newInstance(ViewGroup parent,
                                                   OnPollStarClickListener onPollStarClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_poll_detail_profile, parent, false);
        return new PollHeaderViewHolder(itemView, onPollStarClickListener);
    }

    @Override
    public void onBindView(Poll poll) {
        long creatorId = poll.getCreatorId();
        User creator = TeamInfoLoader.getInstance().getUser(creatorId);
        ProfileBinder.newInstance(tvUserName, vUserNameDisableIndicator,
                ivUserProfile, vgProfileAbsence, vUserProfileDisableIndicator)
                .bind(creator);

        String createdAt = DateTransformator.getTimeString(poll.getCreatedAt());
        tvCreatedDate.setText(createdAt);

        if (!TextUtils.equals("deleted", poll.getStatus())) {
            btnStar.setVisibility(View.VISIBLE);
            btnStar.setSelected(poll.isStarred());
        } else {
            btnStar.setVisibility(View.GONE);
        }

        if (onPollStarClickListener != null) {
            btnStar.setOnClickListener(v -> onPollStarClickListener.onPollStar(poll));
        } else {
            btnStar.setOnClickListener(null);
        }
    }

    public interface OnPollStarClickListener {
        void onPollStar(Poll poll);
    }

}
