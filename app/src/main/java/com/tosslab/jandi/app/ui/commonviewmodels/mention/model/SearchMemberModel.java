package com.tosslab.jandi.app.ui.commonviewmodels.mention.model;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;

/**
 * Created by tee on 15. 7. 22..
 */
@EBean
public class SearchMemberModel {

    LinkedHashMap<Integer, SearchedItemVO> selectableMembersLinkedHashMap;

//    boolean isFirst = true;

    public List<SearchedItemVO> getUserSearchByName(List<Integer> topicIds, String subNameString,
                                                    LinkedHashMap<Integer, SearchedItemVO>
                                                            alreadySelectedMemberHashMap,
                                                    String mentionType) {

        if (topicIds == null || topicIds.size() == 0)
            return new ArrayList<>();

        List<Integer> members = new ArrayList<>();

        Observable.from(topicIds)
                .subscribe(topicId -> Observable
                                .from(EntityManager.getInstance(JandiApplication.getContext())
                                        .getEntityById(topicId).getMembers())
                                .subscribe(member -> {
                                            if (!members.contains(member)) {
                                                members.add(member);
                                            }
                                        }
                                )
                );

        List<FormattedEntity> usersWithoutMe = EntityManager.getInstance(JandiApplication.getContext())
                .getFormattedUsersWithoutMe();

        List<SearchedItemVO> searchedItems = new ArrayList<>();

        Iterator<SearchedItemVO> iterator = Observable.from(members)
                .map(memberId -> Observable.from(usersWithoutMe)
                        .filter(entity -> !TextUtils.isEmpty(entity.getName())
                                && entity.getName().toLowerCase().contains(subNameString.toLowerCase())
                                && entity.getId() == memberId)
                        .map(entity -> {
                            SearchedItemVO searchedItem = new SearchedItemVO();
                            searchedItem.setName(entity.getName())
                                    .setId(entity.getId())
                                    .setType("member")
                                    .setSmallProfileImageUrl(entity.getUserSmallProfileUrl())
                                    .setEnabled(TextUtils.equals(entity.getUser().status, "enabled"))
                                    .setStarred(entity.isStarred);
                            return searchedItem;
                        })
                        .toBlocking()
                        .firstOrDefault(new SearchedItemVO().setId(-1)))
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            SearchedItemVO searchedItem = iterator.next();
            if (alreadySelectedMemberHashMap == null ||
                    !alreadySelectedMemberHashMap.containsKey(searchedItem.getId())) {
                if (searchedItem.getId() != -1) {
                    searchedItems.add(searchedItem);
                }
            }
        }

        if (mentionType.equals(MentionControlViewModel.MENTION_TYPE_MESSAGE)
                && alreadySelectedMemberHashMap != null
                && !alreadySelectedMemberHashMap.containsKey(topicIds.get(0))) {
            if (members.size() - 1 > 0 && "All".toLowerCase().contains(subNameString.toLowerCase())) {
                SearchedItemVO searchedItemForAll = new SearchedItemVO();
                searchedItemForAll
                        .setId(topicIds.get(0))
                        .setName("All")
                        .setType("room");
                searchedItems.add(searchedItemForAll);
            }
        }

        Collections.sort(searchedItems, getChatItemComparator());


//        if (isFirst) {
//        setSelectableMembersLinkedHashMap(searchedItems);
//            isFirst = false;
//        }

        return searchedItems;

    }

    public void refreshSelectableMembers(List<Integer> topicIds, String mentionType) {

        if (topicIds == null || topicIds.size() == 0)
            return;

        List<Integer> members = new ArrayList<>();

        Observable.from(topicIds)
                .subscribe(topicId -> Observable
                                .from(EntityManager.getInstance(JandiApplication.getContext())
                                        .getEntityById(topicId).getMembers())
                                .subscribe(member -> {
                                            if (!members.contains(member)) {
                                                members.add(member);
                                            }
                                            Log.e("memberId", member + "");
                                        }
                                )
                );

        List<FormattedEntity> usersWithoutMe = EntityManager.getInstance(JandiApplication.getContext())
                .getFormattedUsersWithoutMe();

        List<SearchedItemVO> searchedItems = new ArrayList<>();

        Iterator<SearchedItemVO> iterator = Observable.from(members)
                .map(memberId -> Observable.from(usersWithoutMe)
                        .filter(entity -> !TextUtils.isEmpty(entity.getName()) && entity.getId() == memberId)
                        .map(entity -> {
                            SearchedItemVO searchedItem = new SearchedItemVO();
                            searchedItem.setName(entity.getName())
                                    .setId(entity.getId())
                                    .setType("member")
                                    .setSmallProfileImageUrl(entity.getUserSmallProfileUrl())
                                    .setEnabled(TextUtils.equals(entity.getUser().status, "enabled"))
                                    .setStarred(entity.isStarred);
                            Log.e("memberName", searchedItem.getName());
                            return searchedItem;
                        })
                        .toBlocking()
                        .firstOrDefault(new SearchedItemVO().setId(-1)))
                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            SearchedItemVO searchedItem = iterator.next();
            if (searchedItem.getId() != -1) {
                searchedItems.add(searchedItem);
            }
        }

        if (mentionType.equals(MentionControlViewModel.MENTION_TYPE_MESSAGE)) {
            SearchedItemVO searchedItemForAll = new SearchedItemVO();
            searchedItemForAll
                    .setId(topicIds.get(0))
                    .setName("All")
                    .setType("room");
            searchedItems.add(searchedItemForAll);
        }

        setSelectableMembersLinkedHashMap(searchedItems);

    }

    private Comparator<SearchedItemVO> getChatItemComparator() {
        return (lhs, rhs) -> {
            int compareValue = 0;
            if (lhs.isEnabled()) {
                if (rhs.isEnabled()) {
                    compareValue = 0;
                } else {
                    compareValue = -1;
                }
            } else {
                if (rhs.isEnabled()) {
                    compareValue = 1;
                } else {
                    compareValue = 0;
                }
            }

            if (compareValue != 0) {
                return compareValue;
            }

            if (lhs.isStarred()) {
                if (rhs.isStarred()) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return -1;
                }
            } else {
                if (rhs.isStarred()) {
                    return 1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }
        };
    }

    public LinkedHashMap<Integer, SearchedItemVO> getSelectableMembers(List<Integer> topicIds, String mentionType) {
        refreshSelectableMembers(topicIds, mentionType);
        return selectableMembersLinkedHashMap;
    }

    public void setSelectableMembersLinkedHashMap(List<SearchedItemVO> searchedMemberList) {
        selectableMembersLinkedHashMap = new LinkedHashMap<>();
        for (SearchedItemVO searchedMember : searchedMemberList) {
            selectableMembersLinkedHashMap.put(searchedMember.getId(), searchedMember);
        }
    }

}