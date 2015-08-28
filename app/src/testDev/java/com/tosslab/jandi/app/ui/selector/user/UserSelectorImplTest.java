package com.tosslab.jandi.app.ui.selector.user;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by jsuch2362 on 15. 8. 18..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class UserSelectorImplTest {

    private UserSelectorImpl userSelector;

    @Before
    public void setUp() throws Exception {
        userSelector = new UserSelectorImpl();

        BaseInitUtil.initData(RuntimeEnvironment.application);

        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance()
                .getInfosForSideMenuByMainRest(AccountRepository.getRepository().getSelectedTeamId());

        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);

    }

    @Test
    public void testGetUsers() throws Exception {

        List<FormattedEntity> formattedEntityList = new ArrayList<>();
        userSelector.getUsers().subscribe(formattedEntityList::addAll);

        assertThat(formattedEntityList.size(), is(greaterThan(0)));
        assertThat(formattedEntityList.get(0).getId(), is(equalTo(EntityManager.getInstance
                (JandiApplication.getContext()).getMe().getId())));


    }
}