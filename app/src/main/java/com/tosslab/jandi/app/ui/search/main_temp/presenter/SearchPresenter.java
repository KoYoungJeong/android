package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterDataModel;

import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchPresenter {

    void sendSearchQuery(String keyword);

    void setSearchAdapterDataModel(SearchAdapterDataModel searchAdapterDataModel);

    void setChangeIsShowUnjoinedTopic(boolean isShowUnjoinedTopic);

    void sendMoreResults();

    void sendSearchHistory();

    void upsertKeywordHistory(String keyword);

    List<String> getOldQueryList(String keyword);

    void onDeleteaAllHistoryItem();

    void onDeleteaHistoryItemByKeyword(String keyword);

    void onLaunchTopicRoom(long topicId, boolean isJoined);

    void onJoinTopic(long topicId, int topicType);

    void onRoomChanged(long roomId, boolean isDirectMessageRoom);

    void onWriterChanged(long writerId);

    void onAccessTypeChanged(String accessType);

    interface View {

        void refreshSearchedAll();

        void refreshHistory();

        void showMoreProgressBar();

        void dismissMoreProgressBar();

        void moveToMessageActivity(long entityId, int entityType);

        void showTopicInfoDialog(TopicRoom topicRoom);

        void moveToPollActivity(long pollId);

        void moveToFileActivity(long messageId, long fileId);

        void moveToMessageActivityFromSearch(long entityId, int entityType, long linkId);

    }
}
