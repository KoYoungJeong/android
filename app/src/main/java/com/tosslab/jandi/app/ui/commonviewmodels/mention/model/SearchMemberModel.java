package com.tosslab.jandi.app.ui.commonviewmodels.mention.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func2;

/**
 * Created by tee on 15. 7. 22..
 */
@EBean
public class SearchMemberModel {


    LinkedHashMap<Integer, SearchedItemVO> selectableMembersLinkedHashMap;

    public List<SearchedItemVO> getUserSearchByName(String subNameString,
                                                    LinkedHashMap<Integer, SearchedItemVO>
                                                            alreadySelectedMemberHashMap) {

        LinkedHashMap<Integer, SearchedItemVO> searchedItemsHashMap;
        List<SearchedItemVO> searchedItems = new ArrayList<>();

        if (selectableMembersLinkedHashMap == null || selectableMembersLinkedHashMap.size() == 0) {
            return searchedItems;
        }

        searchedItemsHashMap =
                (LinkedHashMap<Integer, SearchedItemVO>) selectableMembersLinkedHashMap.clone();

        Observable.from(searchedItemsHashMap.keySet())
                .filter(integer -> alreadySelectedMemberHashMap != null &&
                        !alreadySelectedMemberHashMap.containsKey(integer))
                .filter(integer ->
                        selectableMembersLinkedHashMap.get(integer).getName().toLowerCase()
                                .contains(subNameString.toLowerCase()))
                .map(selectableMembersLinkedHashMap::get)
                .toSortedList(getChatItemComparator())
                .subscribe(searchedItems::addAll);

        return searchedItems;

    }

    public LinkedHashMap<Integer, SearchedItemVO> refreshSelectableMembers
            (List<Integer> topicIds, String mentionType) {

        selectableMembersLinkedHashMap = new LinkedHashMap<Integer, SearchedItemVO>();

        List<FormattedEntity> usersWithoutMe = EntityManager.getInstance()
                .getFormattedUsersWithoutMe();

        Observable.from(topicIds)
                .flatMap(topicId -> Observable.from(EntityManager.getInstance()
                        .getEntityById(topicId).getMembers()))
                .collect((Func0<ArrayList<Integer>>) ArrayList::new, (members, memberId) -> {
                    if (!members.contains(memberId)) {
                        members.add(memberId);
                    }
                })
                .flatMap(Observable::from)
                .flatMap(memberId -> Observable.from(usersWithoutMe)
                        .filter(entity -> !TextUtils.isEmpty(entity.getName()) && entity.getId() == memberId))
                .map(entity -> new SearchedItemVO().setName(entity.getName())
                        .setId(entity.getId())
                        .setType("member")
                        .setSmallProfileImageUrl(entity.getUserSmallProfileUrl())
                        .setEnabled(TextUtils.equals(entity.getUser().status, "enabled"))
                        .setStarred(entity.isStarred))
                .collect(() -> selectableMembersLinkedHashMap,
                        (selectableMembersLinkedHashMap, searchedItem) -> selectableMembersLinkedHashMap.put(searchedItem.getId(), searchedItem))
                .subscribe();

        if (mentionType.equals(MentionControlViewModel.MENTION_TYPE_MESSAGE)) {
            SearchedItemVO searchedItemForAll = new SearchedItemVO();
            searchedItemForAll
                    .setId(topicIds.get(0))
                    .setName("All")
                    .setType("room");
            selectableMembersLinkedHashMap.put(searchedItemForAll.getId(), searchedItemForAll);
        }

        return selectableMembersLinkedHashMap;

    }

    public LinkedHashMap<Integer, SearchedItemVO> getAllSelectableMembers() {
        return selectableMembersLinkedHashMap;
    }

    private Func2<SearchedItemVO, SearchedItemVO, Integer> getChatItemComparator() {
        return (lhs, rhs) -> {
            if (TextUtils.equals(lhs.getName(), "room")) {
                return -1;
            } else if (TextUtils.equals(rhs.getName(), "room")) {
                return 1;
            } else {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        };
    }
}