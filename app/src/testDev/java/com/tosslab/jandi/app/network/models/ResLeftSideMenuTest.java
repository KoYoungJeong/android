package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RestAdapterBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class ResLeftSideMenuTest {

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);
        ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getAccountTeams().get(0);
        AccountRepository.getRepository().updateSelectedTeamInfo(userTeam.getTeamId());

    }


    @Test
    public void testGetLeftSideMenu() throws Exception {
        int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

        ResLeftSideMenu infosForSideMenu = RestAdapterBuilder.newInstance(MainRestApiClient.class).create()
                .getInfosForSideMenu(selectedTeamId);

        OrmDatabaseHelper helper = OpenHelperManager.getHelper(Robolectric.application, OrmDatabaseHelper.class);

        Dao<ResLeftSideMenu.Channel, ?> dao = helper.getDao(ResLeftSideMenu.Channel.class);
        Dao<ResLeftSideMenu.PublicTopicRef, ?> topicRefDao = helper.getDao(ResLeftSideMenu.PublicTopicRef.class);

        Observable.from(infosForSideMenu.joinEntities)
                .filter(entity -> entity instanceof ResLeftSideMenu.Channel)
                .subscribe(entity -> {
                    try {
                        ResLeftSideMenu.Channel entity1 = (ResLeftSideMenu.Channel) entity;
                        for (ResLeftSideMenu.PublicTopicRef ch_member : entity1.ch_members) {
                            ch_member.channel = entity1;

                            topicRefDao.create(ch_member);
                        }
                        dao.create(entity1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        List<ResLeftSideMenu.Channel> channels = dao.queryForAll();

        assertThat(channels, is(notNullValue()));
        assertTrue(channels.get(0).ch_members.size() > 0);

    }

}