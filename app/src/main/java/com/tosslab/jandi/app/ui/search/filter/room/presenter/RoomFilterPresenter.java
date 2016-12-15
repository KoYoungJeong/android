package com.tosslab.jandi.app.ui.search.filter.room.presenter;

/**
 * Created by tonyjs on 2016. 7. 29..
 */
public interface RoomFilterPresenter {

    void initTopicSearchQueue();

    void initUserSearchQueue();

    void onInitializeRooms(RoomType roomType);

    void onSearchRooms(String query, RoomType roomType);

    void stopDirectMessageSearchQueue();

    void stopTopicSearchQueue();

    void onRoomTypeChanged(RoomType roomType, String enteredQuery);

    void onMemberClickActionForGetRoomId(long memberId);

    void onInitializeSelectedRoomId(boolean isTopic, long selectedRoomId);

    void setShowDefaultTopic(boolean showDefaultTopic);

    enum RoomType {
        Topic, DirectMessage
    }

    interface View {
        void showProgress();

        void hideProgress();

        void notifyDataSetChanged();

        void setResult(boolean b, long roomId, long memberId);

        void finish();
    }
}
