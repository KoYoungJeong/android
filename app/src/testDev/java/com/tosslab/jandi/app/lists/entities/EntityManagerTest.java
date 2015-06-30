package com.tosslab.jandi.app.lists.entities;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricGradleTestRunner.class)
public class EntityManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);

        int teamId = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0).getTeamId();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(teamId);

        JandiEntityClient client = JandiEntityClient_.getInstance_(Robolectric.application);
        ResLeftSideMenu totalEntitiesInfo = client.getTotalEntitiesInfo();

        JandiEntityDatabaseManager manager = JandiEntityDatabaseManager.getInstance(Robolectric.application);
        manager.upsertLeftSideMenu(totalEntitiesInfo);

    }

    @Test
    public void testThreadSafe() throws Exception {

        int teamId = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0).getTeamId();
        ResLeftSideMenu entityInfoAtWhole = JandiEntityDatabaseManager.getInstance(Robolectric.application)
                .getEntityInfoAtWhole(teamId);

        int initThreadCount = Thread.activeCount();
        System.out.println(initThreadCount + " Threads Alive");

        for (int idx = 0; idx < 100; ++idx) {
            new Thread(() -> {
                EntityManager entityManager = EntityManager.getInstance(Robolectric.application);
                System.out.println(Thread.currentThread().getName() + " : Refresh Start");
                entityManager.refreshEntity(entityInfoAtWhole);
                System.out.println(Thread.currentThread().getName() + " : Refresh End");

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(Thread.currentThread().getName() + " : Load Start");
                entityManager.getMe();
                entityManager.getCategorizableEntities();
                entityManager.getDefaultTopicId();
                entityManager.getDistictId();
                entityManager.getEntityById(entityInfoAtWhole.user.id);
                entityManager.getEntityNameById(entityInfoAtWhole.user.id);
                entityManager.getFormattedUsers();
                entityManager.getFormattedUsersWithoutMe();
                entityManager.getGroups();
                entityManager.getJoinedChannels();
                entityManager.getTeamId();
                entityManager.getTeamName();
                entityManager.getUnjoinedChannels();
                entityManager.getMe();
                entityManager.getUnjoinedMembersOfEntity(entityInfoAtWhole.joinEntities.get(0).id,
                        JandiConstants.TYPE_PUBLIC_TOPIC);
                entityManager.isMe(entityInfoAtWhole.user.id);
                entityManager.isMyTopic(entityInfoAtWhole.user.id);
                entityManager.retrieveAccessableEntities();
                entityManager.retrieveExclusivedEntities(
                        Arrays.asList(entityInfoAtWhole.joinEntities.get(0).id));
                entityManager.retrieveGivenEntities(
                        Arrays.asList(entityInfoAtWhole.joinEntities.get(0).id));

                System.out.println(Thread.currentThread().getName() + " : Load End");

                FormattedEntity me = entityManager.getMe();
                assertThat(me, is(notNullValue()));

            }).start();
        }

        await().timeout(1, TimeUnit.HOURS).until(() -> initThreadCount <= Thread.activeCount());
        System.out.println(Thread.activeCount() + " Threads Alive");

    }
}