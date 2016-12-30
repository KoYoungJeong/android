package com.tosslab.jandi.app.ui.search.main.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.search.main.object.SearchOneToOneRoomData;
import com.tosslab.jandi.app.ui.search.main.object.SearchTopicRoomData;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tee on 16. 7. 20..
 */
public class SearchModel {

    private final Lazy<SearchApi> searchApi;

    @Inject
    public SearchModel(Lazy<SearchApi> searchApi) {
        this.searchApi = searchApi;
    }

    public ResSearch searchMessages(ReqSearch reqSearch) throws RetrofitException {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        return searchApi.get().getSearch(teamId, reqSearch);
    }

    public List<SearchTopicRoomData> getSearchedTopics(String keyword, boolean isShowUnjoinedTopic) {
        List<SearchTopicRoomData> topics = new ArrayList<>();
        List<TopicRoom> topicList = TeamInfoLoader.getInstance().getTopicList();
        boolean guest = TeamInfoLoader.getInstance().getMyLevel() == Level.Guest;
        Observable.from(topicList)
                .map(topicRoom -> new SearchTopicRoomData.Builder()
                        .setTopicId(topicRoom.getId())
                        .setTitle(topicRoom.getName())
                        .setMemberCnt(topicRoom.getMembers().size())
                        .setIsPublic(topicRoom.getType().equals("channel"))
                        .setIsJoined(topicRoom.isJoined())
                        .setIsStarred(topicRoom.isStarred())
                        .setDescription(topicRoom.getDescription())
                        .setKeyword(keyword)
                        .build())
                .filter(topic -> {
                    if (!isShowUnjoinedTopic || guest) {
                        return topic.isJoined();
                    } else {
                        return true;
                    }
                })
                .filter(topic -> topic.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .toSortedList((lhs, rhs) -> {
                    if (lhs.isJoined() && rhs.isJoined()) {
                        if (lhs.isStarred() && rhs.isStarred()) {
                            return StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle());
                        } else if (lhs.isStarred()) {
                            return -1;
                        } else if (rhs.isStarred()) {
                            return 1;
                        } else {
                            return StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle());
                        }
                    } else if (lhs.isJoined()) {
                        return -1;
                    } else if (rhs.isJoined()) {
                        return 1;
                    } else {
                        if (lhs.isStarred() && rhs.isStarred()) {
                            return StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle());
                        } else if (lhs.isStarred()) {
                            return -1;
                        } else if (rhs.isStarred()) {
                            return 1;
                        } else {
                            return StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle());
                        }
                    }
                })
                .collect(() -> topics, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);

        return topics;
    }

    public List<SearchOneToOneRoomData> getSearchedOneToOneRoom(String keyword) {


        List<SearchOneToOneRoomData> searchOneToOneRoomDatas = new ArrayList<>();
        Observable.from(getUsers())
                .filter(User::isEnabled)
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(user -> user.getId() != TeamInfoLoader.getInstance().getMyId())
                .map(user -> new SearchOneToOneRoomData.Builder()
                        .setKeyword(keyword)
                        .setUserProfileUrl(user.getPhotoUrl())
                        .setMemberId(user.getId())
                        .setTitle(user.getName())
                        .setUserProfileUrl(user.getPhotoUrl())
                        .build())
                .toSortedList((lhs, rhs) ->
                        StringCompareUtil.compare(lhs.getTitle(), rhs.getTitle()))
                .collect(() -> searchOneToOneRoomDatas, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);

        return searchOneToOneRoomDatas;

    }

    private List<User> getUsers() {
        if (TeamInfoLoader.getInstance().getMyLevel() == Level.Guest) {
            List<User> userList = new ArrayList<>();

            Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .filter(TopicRoom::isJoined)
                    .concatMap(topic -> Observable.from(topic.getMembers()))
                    .distinct()
                    .map(memberId -> TeamInfoLoader.getInstance().getUser(memberId))
                    .collect(() -> userList, List::add)
                    .subscribe();
            return userList;
        } else {
            return TeamInfoLoader.getInstance().getUserList();
        }

    }

    public long upsertSearchQuery(String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .upsertSearchKeyword(text);
    }

    public List<String> getHistory() {
        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .getSearchAllHistory();
    }

    public List<String> searchOldQuery(String text) {
        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .getSearchKeywords(text);
    }

    public void removeHistoryItemByKeyword(String keyword) {
        JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .removeItemByKeyword(keyword);
    }

    public void removeHistoryAllItems() {
        JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .removeAllItems();
    }

    public TopicRoom getTopicRoomById(long topicId) {
        return TeamInfoLoader.getInstance().getTopic(topicId);
    }

    public void joinTopic(long topicId) throws RetrofitException {
        EntityClientManager entityClientManager
                = EntityClientManager_.getInstance_(JandiApplication.getContext());
        entityClientManager.joinChannel(topicId);
        TopicRepository.getInstance().updateTopicJoin(topicId, true);
        TeamInfoLoader.getInstance().refresh();
    }

    public String getWriterName(long writerId) {
        return TeamInfoLoader.getInstance().getMemberName(writerId);
    }

    public boolean isDirectRoomByRoomId(long roomId) {
        return TeamInfoLoader.getInstance().isChat(roomId);
    }

    public boolean isMessageLimited() {
        boolean isLimited = TeamInfoLoader.getInstance().getTeamPlan().isExceedMessage();
        return isLimited;

    }

    public boolean isShowAllRoom() {
        return TeamInfoLoader.getInstance().getMyLevel() != Level.Guest;
    }

    public boolean isGuest() {
        return TeamInfoLoader.getInstance().getMyLevel() == Level.Guest;
    }

    public boolean isOpenedOfChatUser(long memberId) {
        long chatId = TeamInfoLoader.getInstance().getChatId(memberId);
        if (chatId > 0) {
            return TeamInfoLoader.getInstance().getChat(chatId).isJoined();
        } else {
            return false;
        }
    }
}