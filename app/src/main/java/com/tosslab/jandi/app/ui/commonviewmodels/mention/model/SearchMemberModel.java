package com.tosslab.jandi.app.ui.commonviewmodels.mention.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.events.entities.MentionableMembersRefreshEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

@EBean
public class SearchMemberModel {


    Map<Long, SearchedItemVO> selectableMembersLinkedHashMap = new ConcurrentHashMap<>();

    public List<SearchedItemVO> getUserSearchByName(String subNameString) {

        List<SearchedItemVO> searchedItems = new ArrayList<>();

        if (selectableMembersLinkedHashMap == null || selectableMembersLinkedHashMap.size() == 0) {
            return searchedItems;
        }

        Map<Long, SearchedItemVO> searchedItemsHashMap = new HashMap<>(selectableMembersLinkedHashMap);

        Observable.from(searchedItemsHashMap.keySet())
                .filter(integer ->
                        selectableMembersLinkedHashMap.get(integer).getName().toLowerCase()
                                .contains(subNameString.toLowerCase()))
                .map(selectableMembersLinkedHashMap::get)
                .toSortedList(getChatItemComparator())
                .subscribe(searchedItems::addAll, Throwable::printStackTrace);

        return searchedItems;

    }

    public void refreshSelectableMembers(long teamId,
                                         List<Long> topicIds,
                                         String mentionType, final Action1<Map<Long, SearchedItemVO>> action) {

        selectableMembersLinkedHashMap.clear();


        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance(teamId);

        long myId = teamInfoLoader.getMyId();

        Observable.from(topicIds)
                .flatMap(topicId -> Observable.from(teamInfoLoader.getTopic(topicId).getMembers()))
                .filter(it -> it != myId)
                .distinct()
                .map(memberId -> TeamInfoLoader.getInstance().getUser(memberId))
                .filter(it -> !TextUtils.isEmpty(it.getName()))
                .map(entity -> new SearchedItemVO().setName(entity.getName())
                        .setId(entity.getId())
                        .setType(SearchType.member.name())
                        .setSmallProfileImageUrl(entity.getPhotoUrl())
                        .setInactive(entity.isInactive())
                        .setEnabled(entity.isEnabled())
                        .setStarred(teamInfoLoader.isStarredUser(entity.getId())))
                .subscribe(searchedItem -> {
                    selectableMembersLinkedHashMap.put(searchedItem.getId(), searchedItem);
                }, Throwable::printStackTrace, () -> {

                    if (TextUtils.equals(mentionType, MentionControlViewModel.MENTION_TYPE_MESSAGE)) {
                        SearchedItemVO searchedItemForAll = new SearchedItemVO();
                        searchedItemForAll
                                .setId(topicIds.get(0))
                                .setName("all")
                                .setType(SearchType.room.name());

                        selectableMembersLinkedHashMap.put(searchedItemForAll.getId(), searchedItemForAll);
                    }

                    EventBus.getDefault().post(new MentionableMembersRefreshEvent());
                    if (action != null) {
                        action.call(selectableMembersLinkedHashMap);
                    }
                });

    }

    public Map<Long, SearchedItemVO> getAllSelectableMembers() {
        return selectableMembersLinkedHashMap;
    }

    public void clear() {
        selectableMembersLinkedHashMap.clear();
    }

    private Func2<SearchedItemVO, SearchedItemVO, Integer> getChatItemComparator() {
        return (lhs, rhs) -> {

            String roomType = SearchType.room.name();
            if (TextUtils.equals(lhs.getType(), roomType)) {
                return -1;
            } else if (TextUtils.equals(rhs.getType(), roomType)) {
                return 1;
            } else if (lhs.isBot()) {
                return -1;
            } else if (rhs.isBot()) {
                return 1;
            } else {
                return StringCompareUtil.compare(lhs.getName(), rhs.getName());
            }
        };
    }

    private enum SearchType {
        room, member
    }
}