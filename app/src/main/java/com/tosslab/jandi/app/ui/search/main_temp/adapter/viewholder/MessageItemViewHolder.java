package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageData;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class MessageItemViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.iv_profile)
    ImageView ivProfile;

    @Bind(R.id.tv_time)
    TextView tvTime;

    @Bind(R.id.tv_user_name)
    TextView tvUserName;

    @Bind(R.id.tv_message_content)
    TextView tvMessageContent;

    @Bind(R.id.tv_room_name)
    TextView tvRoomName;

    @Bind(R.id.tv_divide_bar)
    TextView tvDivideBar;

    @Bind(R.id.iv_shared_item_icon)
    ImageView ivSharedItemIcon;

    @Bind(R.id.tv_shared_item_title)
    TextView tvSharedItemTitle;

    public MessageItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MessageItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_message_item, parent, false);
        return new MessageItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        SearchMessageData searchMessageData = (SearchMessageData) searchData;
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        User writer = teamInfoLoader.getUser(searchMessageData.getWriterId());

        if (writer != null) {
            String photoUrl =
                    teamInfoLoader.getUser(searchMessageData.getWriterId()).getPhotoUrl();
            ImageUtil.loadProfileImage(ivProfile, photoUrl, R.drawable.profile_img);
        }

        tvTime.setText(DateTransformator.getTimeString(searchMessageData.getCreatedAt()));

        tvUserName.setText(teamInfoLoader.getMemberName(searchMessageData.getWriterId()));

        tvMessageContent.setText(searchMessageData.getText());

        long roomId = searchMessageData.getRoomId();

        if (teamInfoLoader.isChat(roomId)) {
            tvRoomName.setText("1:1");
        } else if (teamInfoLoader.isTopic(roomId)) {
            tvRoomName.setText(teamInfoLoader.getTopic(roomId).getName());
        }

        String feedbackType = searchMessageData.getFeedbackType();

        if (TextUtils.isEmpty(feedbackType)) {
            tvDivideBar.setVisibility(View.GONE);
            ivSharedItemIcon.setVisibility(View.GONE);
            tvSharedItemTitle.setVisibility(View.GONE);
        } else {
            tvDivideBar.setVisibility(View.VISIBLE);
            ivSharedItemIcon.setVisibility(View.VISIBLE);
            tvSharedItemTitle.setVisibility(View.VISIBLE);
            if (feedbackType.equals("file") && searchMessageData.getFile() != null) {
                ivSharedItemIcon.setImageDrawable(JandiApplication.getContext().getResources()
                        .getDrawable(R.drawable.account_icon_upload));
                tvSharedItemTitle.setText(searchMessageData.getFile().getTitle());
            } else if (feedbackType.equals("poll") && searchMessageData.getPoll() != null) {
                ivSharedItemIcon.setImageDrawable(JandiApplication.getContext().getResources()
                        .getDrawable(R.drawable.account_icon_poll));
                tvSharedItemTitle.setText(searchMessageData.getPoll().getSubject());
            }
        }

    }
}