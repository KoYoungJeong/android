package com.tosslab.jandi.app.ui.commonviewmodels.mention.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel_;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 2015. 11. 3..
 */
@RunWith(AndroidJUnit4.class)
public class SearchMemberModelTest {

    SearchMemberModel searchMemberModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();

        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        AccountRepository.getRepository().updateSelectedTeamInfo(accountTeams.get(0).getTeamId());

        ShareSelectModel shareSelectModel = ShareSelectModel_.getInstance_(JandiApplication.getContext());

        Observable.from(accountTeams)
                .subscribe(userTeam -> {
                    ResLeftSideMenu leftSideMenu = shareSelectModel.getLeftSideMenu(userTeam.getTeamId());
                    LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
                });

        searchMemberModel = SearchMemberModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testRefreshSelectableMembers() throws Exception {

        ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getAccountTeams().get(0);
        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(userTeam.getTeamId());

        LinkedHashMap<Integer, SearchedItemVO> searchedItemVOLinkedHashMap = searchMemberModel.refreshSelectableMembers(userTeam.getTeamId(), Arrays.asList(leftSideMenu.team.t_defaultChannelId), MentionControlViewModel.MENTION_TYPE_MESSAGE);

        assertThat(searchedItemVOLinkedHashMap.size(), is(greaterThan(0)));

        EntityManager manager = EntityManager.getInstance();
        LinkedHashMap<Integer, SearchedItemVO> integerSearchedItemVOLinkedHashMap = searchMemberModel.refreshSelectableMembers(manager.getTeamId(), Arrays.asList(manager.getDefaultTopicId()), MentionControlViewModel.MENTION_TYPE_MESSAGE);

        assertThat(integerSearchedItemVOLinkedHashMap.size(), is(equalTo(manager.getEntityById(manager.getDefaultTopicId()).getMembers().size())));
    }
}