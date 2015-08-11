package com.tosslab.jandi.app.lists.entities;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;


@RunWith(JandiRobolectricGradleTestRunner.class)
public class EntityManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();


    }

    @Test
    @Ignore
    public void testHasNewChatMessage() throws Exception {

        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(infosForSideMenu);

        EntityManager instance = EntityManager.getInstance(RuntimeEnvironment.application);

        List<FormattedEntity> formattedUsersWithoutMe = instance.getFormattedUsersWithoutMe();

        Boolean allUser = Observable.from(formattedUsersWithoutMe)
                .filter(new Func1<FormattedEntity, Boolean>() {
                    @Override
                    public Boolean call(FormattedEntity entity) {
                        return entity.alarmCount > 0;
                    }
                })
                .firstOrDefault(new FormattedEntity())
                .map(new Func1<FormattedEntity, Boolean>() {
                    @Override
                    public Boolean call(FormattedEntity entity) {
                        return entity.alarmCount > 0;
                    }
                })
                .toBlocking()
                .first();

    }


}