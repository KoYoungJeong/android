package com.tosslab.jandi.app.lists.entities;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;


@RunWith(RobolectricGradleTestRunner.class)
public class EntityManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
    }

    @Test
    @Ignore
    public void testHasNewChatMessage() throws Exception {

        int teamId = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0).getTeamId();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(teamId);

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
        JandiEntityDatabaseManager.getInstance(Robolectric.application).upsertLeftSideMenu(infosForSideMenu);

        EntityManager instance = EntityManager.getInstance(Robolectric.application);

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