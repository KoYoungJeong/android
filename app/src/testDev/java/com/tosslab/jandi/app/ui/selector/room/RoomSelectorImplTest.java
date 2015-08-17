package com.tosslab.jandi.app.ui.selector.room;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Created by jsuch2362 on 15. 8. 17..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class RoomSelectorImplTest {

    private RoomSelectorImpl selectorImpl;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
        int teamId = AccountRepository
                .getRepository().getSelectedTeamId();
        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);

        selectorImpl = RoomSelectorImpl_.getInstance_(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testGetUsers() throws Exception {

        List<FormattedEntity> entities = new ArrayList<>();
        selectorImpl.getUsers().subscribe(entities::addAll);

        assertThat(entities.size(), is(greaterThan(0)));
        assertThat(entities.get(0).type, is(equalTo(FormattedEntity.TYPE_EVERYWHERE)));

        Observable.from(entities.subList(1, entities.size()))
                .subscribe(formattedEntity -> {
                    if (!formattedEntity.isUser()) {
                        fail("It must be User");
                    }
                });


    }

    @Test
    public void testGetTopics() throws Exception {
        List<FormattedEntity> entities = new ArrayList<>();
        selectorImpl.getTopics().subscribe(entities::addAll);

        assertThat(entities.size(), is(greaterThan(0)));
        assertThat(entities.get(0).type, is(equalTo(FormattedEntity.TYPE_EVERYWHERE)));

        Observable.from(entities.subList(1, entities.size()))
                .subscribe(formattedEntity -> {
                    if (formattedEntity.isUser()) {
                        fail("It must be User");
                    }
                });

    }
}