package com.tosslab.jandi.app.ui.share.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import rx.Observable;
import setup.BaseInitUtil;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ShareModelTest {

    private ShareModel shareModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        shareModel = ShareModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testGetTeamInfoById() throws Exception {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        ResTeamDetailInfo.InviteTeam inviteTeam = shareModel.getTeamInfoById(teamId);

        ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getTeamInfo(teamId);
        assertThat(inviteTeam.getTeamDomain(), is(equalTo(userTeam.getTeamDomain())));
        assertThat(inviteTeam.getName(), is(equalTo(userTeam.getName())));
    }

    @Test
    public void testSendMessage() throws Exception {

        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();

        ResCommon result = shareModel.sendMessage(teamId, topicId, JandiConstants.TYPE_PUBLIC_TOPIC, "hello", new ArrayList<>());
        assertThat(result, is(notNullValue()));
        assertThat(result.id, is(greaterThan(0L)));
    }

    @Test
    public void testHasLeftSideMenu() throws Exception {
        boolean hasLeftSideMenu = shareModel.hasLeftSideMenu(1);
        assertThat(hasLeftSideMenu, is(false));

        hasLeftSideMenu = shareModel.hasLeftSideMenu(TeamInfoLoader.getInstance().getTeamId());
        assertThat(hasLeftSideMenu, is(true));
    }

    @Test
    public void testGetLeftSideMenu() throws Exception {
        InitialInfo leftSideMenu = shareModel.getInitialInfo(TeamInfoLoader.getInstance().getTeamId());
        assertThat(leftSideMenu, is(notNullValue()));
    }

    @Test
    public void testUpdateLeftSideMenu() throws Exception {
        long teamId = Observable.from(AccountRepository.getRepository().getAccountTeams())
                .filter(userTeam -> userTeam.getTeamId() != AccountRepository.getRepository().getSelectedTeamId())
                .map(ResAccountInfo.UserTeam::getTeamId)
                .firstOrDefault(-1L)
                .toBlocking().first();

        if (teamId > 0) {
            InitialInfo leftSideMenu = shareModel.getInitialInfo(teamId);
            boolean success = shareModel.updateInitialInfo(leftSideMenu);

            assertThat(success, is(true));
        }
    }

    @Test
    public void testGetShareSelectModel() throws Exception {
        TeamInfoLoader shareSelectModel = shareModel.getTeamInfoLoader(AccountRepository.getRepository().getSelectedTeamId());
        assertThat(shareSelectModel.getDefaultTopicId(), is(equalTo(TeamInfoLoader.getInstance().getDefaultTopicId())));
    }
}