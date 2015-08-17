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

    public List<SearchedItemVO> getUserSearchByName(String subNameString,
                                                    LinkedHashMap<Integer, SearchedItemVO>
                                                            alreadySelectedMemberHashMap) {

        LinkedHashMap<Integer, SearchedItemVO> searchedItemsHashMap;
        List<SearchedItemVO> searchedItems = new ArrayList<>();

        if (selectableMembersLinkedHashMap == null || selectableMembersLinkedHashMap.size() == 0) {
            return searchedItems;
        } else {
            searchedItemsHashMap =
                    (LinkedHashMap<Integer, SearchedItemVO>) selectableMembersLinkedHashMap.clone();
        }

        if (alreadySelectedMemberHashMap != null) {
            Iterator selectableMembersKeySet = alreadySelectedMemberHashMap.keySet().iterator();
            while (selectableMembersKeySet.hasNext()) {
                Integer key = (Integer) selectableMembersKeySet.next();
                searchedItemsHashMap.remove(key);
            }
        }

        Iterator searchedItemsKeySet = searchedItemsHashMap.keySet().iterator();

        while (searchedItemsKeySet.hasNext()) {
            Integer key = (Integer) searchedItemsKeySet.next();
            if (selectableMembersLinkedHashMap.get(key).getName()
                    .toLowerCase().contains(subNameString.toLowerCase())) {
                searchedItems.add(selectableMembersLinkedHashMap.get(key));
            }
        }

        Collections.sort(searchedItems, getChatItemComparator());

        return searchedItems;

    }

    public LinkedHashMap<Integer, SearchedItemVO> refreshSelectableMembers
            (List<Integer> topicIds, String mentionType) {

        if (topicIds == null || topicIds.size() == 0) {
            selectableMembersLinkedHashMap = new LinkedHashMap<Integer, SearchedItemVO>();
        }

        List<Integer> members = new ArrayList<>();

        selectableMembersLinkedHashMap
                = new LinkedHashMap<Integer, SearchedItemVO>();

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

        Observable.from(members)
                .subscribe(memberId -> Observable.from(usersWithoutMe)
                        .filter(entity -> !TextUtils.isEmpty(entity.getName()) && entity.getId() == memberId)
                        .subscribe(entity -> {
                            SearchedItemVO searchedItem = new SearchedItemVO();
                            searchedItem.setName(entity.getName())
                                    .setId(entity.getId())
                                    .setType("member")
                                    .setSmallProfileImageUrl(entity.getUserSmallProfileUrl())
                                    .setEnabled(TextUtils.equals(entity.getUser().status, "enabled"))
                                    .setStarred(entity.isStarred);
                            selectableMembersLinkedHashMap.put(searchedItem.getId(), searchedItem);
                        }));

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
}