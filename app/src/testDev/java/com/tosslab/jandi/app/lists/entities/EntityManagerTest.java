package com.tosslab.jandi.app.lists.entities;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricGradleTestRunner.class)
public class EntityManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);

    }

    @Test
    public void testHasNewChatMessage() throws Exception {

        int teamId = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0).getTeamId();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(teamId);

        JandiRestClient jandiRestClient = new JandiRestClient_(Robolectric.application);
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        ResLeftSideMenu infosForSideMenu = jandiRestClient.getInfosForSideMenu(teamId);
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


        List<FormattedEntity> joinedUsers = instance.getJoinedUsers();

        Boolean joinUser = Observable.from(joinedUsers)
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

        boolean hasNewChatMessage = instance.hasNewChatMessage();

        assertThat(joinUser, is(hasNewChatMessage));
    }


}