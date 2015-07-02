package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class MainRestClientTest {

    @Before
    public void setUp() throws Exception {

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    @Test
    public void testGetAccessToken() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        assertNotNull(accessToken);
        System.out.println(accessToken);

    }

    @Test
    public void testGetMyTeam() throws Exception {

        ResMyTeam teamId = RequestApiManager.getInstance().getTeamIdByMainRest(BaseInitUtil.TEST_ID);
        assertNotNull(teamId);
        System.out.println(teamId);

    }

    @Test
    public void testGetLeftSideMenu() throws Exception {


        ResMyTeam teamId = RequestApiManager.getInstance().getTeamIdByMainRest(BaseInitUtil.TEST_ID);
        System.out.println("========= Get Team Info =========");
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId.teamList.get(0).teamId);
        assertNotNull(infosForSideMenu);
        System.out.println(infosForSideMenu);

    }

    @Test
    public void testSearchFile() throws Exception {


        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.teamId = 279;
        reqSearchFile.searchType = "file";
        reqSearchFile.writerId = "288";
        reqSearchFile.sharedEntityId = 281;
        reqSearchFile.fileType = "all";
        reqSearchFile.startMessageId = 3601;
        reqSearchFile.listCount = 10;
        reqSearchFile.keyword = "";
        ResSearchFile resSearchFile = RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);
        assertNotNull(resSearchFile);

        System.out.println(resSearchFile);

    }

    @Test
    public void testGetAccountInfo() throws Exception {

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        assertNotNull(accountInfo);
        System.out.println(accountInfo);

    }

    @Test
    public void testSignUp() throws Exception {

        ReqSignUpInfo reqSignUp = new ReqSignUpInfo("test.test@tosslab.com", "123456Ab~", "test test", "en");
        ResCommon resCommon = RequestApiManager.getInstance().signUpAccountByMainRest(reqSignUp);
        assertNotNull(resCommon);
        System.out.println(resCommon);

    }

    @Ignore
    @Test
    public void testActivateAccount() throws Exception {

        // Cannot test.
        ReqAccountActivate reqAccountActivate = new ReqAccountActivate("aa", "aa");
        ResAccountActivate resAccountActivate = RequestApiManager.getInstance().activateAccountByMainRest(reqAccountActivate);
        assertNull(resAccountActivate);

    }


    @Test
    public void testSetMarker() throws Exception {

        ResCommon resCommon = RequestApiManager.getInstance().setMarkerByMainRest(281, new ReqSetMarker(279, 12554, ReqSetMarker.CHANNEL));
        assertNotNull(resCommon);
        System.out.println(resCommon);

    }

    @Test
    public void testGetConfig() throws Exception {
        ResConfig config = RequestApiManager.getInstance().getConfigByMainRest();
        assertThat(config, is(notNullValue()));
        System.out.println(config);
    }

    private ResAccessToken getAccessToken() {

        ResAccessToken accessToken = RequestApiManager.getInstance().
                getAccessTokenByMainRest(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }
}