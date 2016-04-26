package com.tosslab.jandi.app.ui.commonviewmodels.mention.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel_;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func2;

@EBean
public class SearchMemberModel {


    LinkedHashMap<Long, SearchedItemVO> selectableMembersLinkedHashMap = new LinkedHashMap<>();

    public List<SearchedItemVO> getUserSearchByName(String subNameString) {

        LinkedHashMap<Long, SearchedItemVO> searchedItemsHashMap;
        List<SearchedItemVO> searchedItems = new ArrayList<>();

        if (selectableMembersLinkedHashMap == null || selectableMembersLinkedHashMap.size() == 0) {
            return searchedItems;
        }

        searchedItemsHashMap =
                (LinkedHashMap<Long, SearchedItemVO>) selectableMembersLinkedHashMap.clone();

        Observable.from(searchedItemsHashMap.keySet())
                .filter(integer ->
                        selectableMembersLinkedHashMap.get(integer).getName().toLowerCase()
                                .contains(subNameString.toLowerCase()))
                .map(selectableMembersLinkedHashMap::get)
                .toSortedList(getChatItemComparator())
                .subscribe(searchedItems::addAll, Throwable::printStackTrace);

        return searchedItems;

    }

    public LinkedHashMap<Long, SearchedItemVO> refreshSelectableMembers(long teamId,
                                                                        List<Long> topicIds,
                                                                        String mentionType) {

        selectableMembersLinkedHashMap.clear();

        ShareSelectModel_ shareSelectModel = ShareSelectModel_
                .getInstance_(JandiApplication.getContext());

        ResLeftSideMenu leftSideMenu = LeftSideMenuRepository.getRepository()
                .findLeftSideMenuByTeamId(teamId);

        if (leftSideMenu == null) {
            leftSideMenu = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu();
        }
        shareSelectModel.initFormattedEntities(leftSideMenu);

        List<FormattedEntity> usersWithoutMe = shareSelectModel.getFormattedUsersWithoutMe();

        Observable.from(topicIds)
                .flatMap(topicId -> Observable.from(shareSelectModel.getEntityById(topicId).getMembers()))
                .collect((Func0<ArrayList<Long>>) ArrayList::new, (members, memberId) -> {
                    if (!members.contains(memberId)) {
                        members.add(memberId);
                    }
                })
                .flatMap(Observable::from)
                .flatMap(memberId -> Observable.from(usersWithoutMe)
                        .filter(entity -> !TextUtils.isEmpty(entity.getName()) && entity.getId() == memberId))
                .map(entity -> new SearchedItemVO().setName(entity.getName())
                        .setId(entity.getId())
                        .setType(SearchType.member.name())
                        .setSmallProfileImageUrl(entity.getUserSmallProfileUrl())
                        .setInactive(entity.isInavtived())
                        .setEnabled(entity.isEnabled())
                        .setStarred(entity.isStarred))
                .collect(() -> selectableMembersLinkedHashMap,
                        (selectableMembersLinkedHashMap, searchedItem) -> selectableMembersLinkedHashMap.put(searchedItem.getId(), searchedItem))
                .subscribe(map -> {
                }, Throwable::printStackTrace);

        if (TextUtils.equals(mentionType, MentionControlViewModel.MENTION_TYPE_MESSAGE)) {
            SearchedItemVO searchedItemForAll = new SearchedItemVO();
            searchedItemForAll
                    .setId(topicIds.get(0))
                    .setName("all")
                    .setType(SearchType.room.name());

            selectableMembersLinkedHashMap.put(searchedItemForAll.getId(), searchedItemForAll);
        }


        if (selectableMembersLinkedHashMap.size() > 0
                && EntityManager.getInstance().hasJandiBot()) {
            BotEntity botEntity = (BotEntity) EntityManager.getInstance().getJandiBot();
            if (botEntity.isEnabled()) {
                SearchedItemVO jandiBot = new SearchedItemVO();
                jandiBot.setId(botEntity.getId())
                        .setName(botEntity.getName())
                        .setEnabled(true)
                        .setStarred(false)
                        .setBot(true)
                        .setType(SearchType.member.name());
                selectableMembersLinkedHashMap.put(jandiBot.getId(), jandiBot);
            }
        }

        return selectableMembersLinkedHashMap;

    }

    public LinkedHashMap<Long, SearchedItemVO> getAllSelectableMembers() {
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