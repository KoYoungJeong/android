package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class RoomItemViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.iv_icon)
    ImageView ivIcon;

    @Bind(R.id.iv_info)
    ImageView ivInfo;

    @Bind(R.id.tv_room_name)
    TextView tvRoomName;

    @Bind(R.id.tv_member_cnt)
    TextView tvMemberCnt;

    @Bind(R.id.tv_room_description)
    TextView tvRoomDescription;

    @Bind(R.id.v_full_divider)
    View vFullDivider;

    @Bind(R.id.v_half_divider)
    View vHalfDivider;


    public RoomItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static RoomItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_room_item, parent, false);
        return new RoomItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        SearchTopicRoomData searchTopicRoomData = (SearchTopicRoomData) searchData;

        if (searchTopicRoomData.isPublic()) {
            if (searchTopicRoomData.isStarred()) {
                ivIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
            } else {
                ivIcon.setImageResource(R.drawable.topiclist_icon_topic);
            }
        } else {
            if (searchTopicRoomData.isStarred()) {
                ivIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
            } else {
                ivIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
            }
        }

        tvRoomName.setText(searchTopicRoomData.getTitle());
        tvMemberCnt.setText("(" + searchTopicRoomData.getMemberCnt() + ")");

        if (!searchTopicRoomData.getDescription().isEmpty()) {
            tvRoomDescription.setVisibility(View.VISIBLE);
            tvRoomDescription.setText(searchTopicRoomData.getDescription());
        } else {
            tvRoomDescription.setVisibility(View.GONE);
        }
    }
}
