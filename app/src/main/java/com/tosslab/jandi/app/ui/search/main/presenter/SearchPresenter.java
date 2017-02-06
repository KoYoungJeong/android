package com.tosslab.jandi.app.ui.search.main.presenter;

import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.search.main.object.SearchMessageData;

import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchPresenter {

    void sendSearchQuery(String keyword, boolean isOnlyMessage);

    void setChangeIsShowUnjoinedTopic(boolean isShowUnjoinedTopic);

    void sendMoreResults();

    void sendSearchHistory();

    void upsertKeywordHistory(String keyword);

    void onDeleteaAllHistoryItem();

    void onDeleteaHistoryItemByKeyword(String keyword);

    void onLaunchTopicRoom(long topicId, boolean isJoined);

    void onJoinTopic(long topicId, int topicType, long linkId);

    void onRoomChanged(long roomId, long memberId);

    void onWriterChanged(long writerId);

    void onAccessTypeChanged(String accessType);

    void onSetOnlyMessageMode(boolean onlyMessageMode);

    void onMoveToMessageFromSearch(SearchMessageData searchMessageData);

    void onDestroy();

    void onSearchKeywordChanged(String text);

    void onInitPricingInfo();

    void onRoomSelect();

    void onOneToOneRoomClick(long memberId);

    void addFileSharedEntity(long fileId, List<Integer> shareEntities);

    void removeFileSharedEntity(long fileId, long roomId);

    interface View {

        void refreshSearchedAll();

        void refreshSearchedOnlyMessage();

        void refreshHistory();

        void showMoreProgressBar();

        void dismissMoreProgressBar();

        void moveToMessageActivity(long entityId, int entityType);

        void showTopicInfoDialog(TopicRoom topicRoom);

        void moveDirectMessage(long memberId);

        void showJoinRoomDialog(TopicRoom topicRoom, long linkId);

        void moveToPollActivity(long pollId);

        void moveToMessageActivityFromSearch(long roomId, long entityId, int entityType, long linkId);

        void hideKeyboard();

        void setSearchHints(List<String> keywords);

        void setPricingLimitView(Boolean isLimited);

        void showChooseRoomDialog(boolean showAllRoom);

        void showShouldOpenedUser();
    }
}