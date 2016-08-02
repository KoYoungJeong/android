package com.tosslab.jandi.app.ui.search.filter.room.adapter.view;

/**
 * Created by tonyjs on 2016. 7. 28..
 */
public interface RoomFilterDataView {

    void setOnTopicRoomClickListener(OnTopicRoomClickListener onTopicRoomClickListener);

    void setOnMemberClickListener(OnMemberClickListener onMemberClickListener);

    void notifyDataSetChanged();

    interface OnTopicRoomClickListener {
        void onTopicRoomClick(long roomId);
    }

    interface OnMemberClickListener {
        void onMemberClick(long memberId);
    }

}
