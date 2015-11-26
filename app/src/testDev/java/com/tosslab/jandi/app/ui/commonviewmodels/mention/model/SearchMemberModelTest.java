package com.tosslab.jandi.app.ui.commonviewmodels.mention.model;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 15. 8. 31..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class SearchMemberModelTest {

    private SearchMemberModel searchMemberModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(AccountRepository
                .getRepository().getSelectedTeamId());

        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);

        searchMemberModel = SearchMemberModel_.getInstance_(RuntimeEnvironment
                .application);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testRefreshSelectableMembers() throws Exception {
        List<Integer> topicIds = Arrays.asList(EntityManager.getInstance().getDefaultTopicId());
        LinkedHashMap<Integer, SearchedItemVO> map = searchMemberModel.refreshSelectableMembers(EntityManager.getInstance().getTeamId(),
                topicIds,
                MentionControlViewModel.MENTION_TYPE_MESSAGE);

        assertThat(map.size(), is(greaterThan(0)));
    }

}