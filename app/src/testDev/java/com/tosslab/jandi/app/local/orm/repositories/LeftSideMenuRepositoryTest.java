package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class LeftSideMenuRepositoryTest {

    private ResAccountInfo originAccountInfo;
    private ResLeftSideMenu leftSideMenu;

    @Before
    public void setUp() throws Exception {
        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(
                        BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);
        originAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        AccountRepository.getRepository().upsertAccountAllInfo(originAccountInfo);

        ResAccountInfo.UserTeam userTeam = originAccountInfo.getMemberships().iterator().next();
        AccountRepository.getRepository().updateSelectedTeamInfo(userTeam.getTeamId());

        leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(userTeam.getTeamId());

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testUpsertLeftSideMenu() throws Exception {
        boolean isSuccess = LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);

        assertThat(isSuccess, is(true));

    }

    @Test
    public void testGetCurrentLeftSideMenu() throws Exception {
        boolean isSuccess = LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
        assertThat(isSuccess, is(true));

        ResLeftSideMenu resLeftSideMenu = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu();

        assertThat(leftSideMenu.team.name, is(equalTo(resLeftSideMenu.team.name)));
        assertThat(leftSideMenu.team.id, is(equalTo(resLeftSideMenu.team.id)));
        assertThat(leftSideMenu.user.id, is(equalTo(resLeftSideMenu.user.id)));
        assertThat(leftSideMenu.entities.size(), is(equalTo(resLeftSideMenu.entities.size())));
        assertThat(leftSideMenu.joinEntities.size(), is(equalTo(resLeftSideMenu.joinEntities.size())));
    }
}