package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarredPollViewHolder extends BaseViewHolder<StarredMessage> {

    private ImageView ivProfile;
    private TextView tvWriter;

    private TextView tvPollTitle;
    private TextView tvCreatedTime;

    private View vSemiDivider;
    private View vFullDivider;
    private View vProfileCover;

    private StarredPollViewHolder(View itemView) {
        super(itemView);
        ivProfile = (ImageView) itemView.findViewById(R.id.iv_starred_poll_profile);
        tvWriter = (TextView) itemView.findViewById(R.id.tv_starred_poll_writer);

        tvPollTitle = (TextView) itemView.findViewById(R.id.tv_starred_poll_title);
        tvCreatedTime = (TextView) itemView.findViewById(R.id.tv_starred_poll_create_date);
        vSemiDivider = itemView.findViewById(R.id.v_semi_divider);
        vFullDivider = itemView.findViewById(R.id.v_full_divider);
        vProfileCover = itemView.findViewById(R.id.v_starred_profile_cover);
    }

    public static StarredPollViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_starred_poll, parent, false);
        return new StarredPollViewHolder(itemView);
    }

    @Override
    public void onBindView(StarredMessage starredMessage) {
        User user = TeamInfoLoader.getInstance().getUser(starredMessage.getMessage().writerId);
        StarredMessageProfileBinder.newInstance(tvWriter, ivProfile, vProfileCover)
                .bind(user);

        StarredMessage.Message.Content content = starredMessage.getMessage().content;
        tvPollTitle.setText(content.body);

        String date = DateTransformator.getTimeString(starredMessage.getMessage().createdAt);
        tvCreatedTime.setText(date);

        if (starredMessage.hasSemiDivider()) {
            vSemiDivider.setVisibility(View.VISIBLE);
            vFullDivider.setVisibility(View.GONE);
        } else {
            vFullDivider.setVisibility(View.VISIBLE);
            vSemiDivider.setVisibility(View.GONE);
        }
    }

}
