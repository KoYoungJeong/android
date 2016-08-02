package com.tosslab.jandi.app.ui.search.filter.room.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

/**
 * Created by tonyjs on 2016. 7. 29..
 */
public class TopicRoomViewHolder extends BaseViewHolder<TopicRoom> {

    private ImageView ivIcon;
    private TextView tvName;

    private TopicRoomViewHolder(View itemView) {
        super(itemView);
        tvName = (TextView) itemView.findViewById(R.id.tv_room_filter_topic_name);
        ivIcon = (ImageView) itemView.findViewById(R.id.iv_room_filter_topic_icon);
    }

    public static TopicRoomViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_room_filter_topic, parent, false);
        return new TopicRoomViewHolder(itemView);
    }

    @Override
    public void onBindView(TopicRoom topic) {
        tvName.setText(topic.getName());

        if (topic.isPublicTopic()) {

            int resId = topic.isStarred()
                    ? R.drawable.topiclist_icon_topic_fav
                    : R.drawable.topiclist_icon_topic;

            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivIcon, resId);
        } else {
            int resId = topic.isStarred()
                    ? R.drawable.topiclist_icon_topic_private_fav
                    : R.drawable.topiclist_icon_topic_private;

            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivIcon, resId);
        }
    }

}

