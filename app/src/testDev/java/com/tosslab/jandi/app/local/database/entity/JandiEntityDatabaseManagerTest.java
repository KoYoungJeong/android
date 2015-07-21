package com.tosslab.jandi.app.local.database.entity;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static com.jayway.awaitility.Awaitility.await;

/**
 * Created by Steve SeongUg Jung on 15. 6. 30..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class JandiEntityDatabaseManagerTest {

    @Test
    public void testMultiThread() throws Exception {

        BaseInitUtil.initData(Robolectric.application);

        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);


        ResLeftSideMenu totalEntitiesInfo = RequestApiManager.getInstance()
                .getInfosForSideMenuByMainRest(teamId);

        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);

        for (int idx = 0; idx < 100; ++idx) {
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " : 11111 엔티티 갱신 시작");
                    LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
                    System.out.println(Thread.currentThread().getName() + " : 11111 엔티티 갱신 종료");
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " : 2222 엔티티 로드 시작");
                LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
                System.out.println(Thread.currentThread().getName() + " : 2222 엔티티 로드 종료");

            }).start();
        }

        await().until(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
}