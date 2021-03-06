package com.tosslab.jandi.app.ui.share.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ShareModelTest {

    @Inject
    ShareModel shareModel;

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
        DaggerShareModelTest_TestComponent.builder().build().inject(this);
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

        List<ResMessages.Link> links = shareModel.sendMessage(teamId, topicId, "hello", new ArrayList<>());
        assertThat(links, is(notNullValue()));
    }

    @Test
    public void testHasLeftSideMenu() throws Exception {
        boolean hasLeftSideMenu = shareModel.hasLeftSideMenu(1);
        assertThat(hasLeftSideMenu, is(false));

        hasLeftSideMenu = shareModel.hasLeftSideMenu(TeamInfoLoader.getInstance().getTeamId());
        assertThat(hasLeftSideMenu, is(true));
    }

    @Test
    public void testGetShareSelectModel() throws Exception {
        TeamInfoLoader shareSelectModel = shareModel.getTeamInfoLoader(AccountRepository.getRepository().getSelectedTeamId());
        assertThat(shareSelectModel.getDefaultTopicId(), is(equalTo(TeamInfoLoader.getInstance().getDefaultTopicId())));
    }

    @Component(modules = ApiClientModule.class)
    public interface TestComponent {
        void inject(ShareModelTest test);
    }
}