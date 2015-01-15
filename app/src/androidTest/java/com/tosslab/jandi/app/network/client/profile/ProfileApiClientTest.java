package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ProfileApiClientTest {

    private JandiRestClient jandiRestClient_;
    private ProfileApiClient profileApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        profileApiClient = new ProfileApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        profileApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

    @Test
    public void testUpdateUserProfile() throws Exception {

        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();

        String oldDepartment = sideMenu.user.u_extraData.department;
        String oldPosition = sideMenu.user.u_extraData.position;
        String oldStatusMessage = sideMenu.user.u_statusMessage;
        String oldPhoneNumber = sideMenu.user.u_extraData.phoneNumber;

        reqUpdateProfile.department = "test " + oldDepartment;
        reqUpdateProfile.position = "test " + oldPosition;
        reqUpdateProfile.statusMessage = "test " + oldStatusMessage;
        reqUpdateProfile.phoneNumber = "test " + oldPhoneNumber;

        profileApiClient.updateMemberProfile(sideMenu.user.id, reqUpdateProfile);

        ResLeftSideMenu sideMenu1 = getSideMenu();

        assertThat(sideMenu1.user.u_extraData.phoneNumber, is(equalTo("test " + oldPhoneNumber)));
        assertThat(sideMenu1.user.u_extraData.position, is(equalTo("test " + oldPosition)));
        assertThat(sideMenu1.user.u_extraData.department, is(equalTo("test " + oldDepartment)));
        assertThat(sideMenu1.user.u_statusMessage, is(equalTo("test " + oldStatusMessage)));

        reqUpdateProfile.department = oldDepartment;
        reqUpdateProfile.position = oldPosition;
        reqUpdateProfile.statusMessage = oldStatusMessage;
        reqUpdateProfile.phoneNumber = oldPhoneNumber;

        profileApiClient.updateMemberProfile(sideMenu.user.id, reqUpdateProfile);


    }

    @Test
    public void testUpdateUserEmail() throws Exception {

        ResLeftSideMenu.User user = profileApiClient.updateMemberEmail(sideMenu.user.id, new ReqAccountEmail("jsuch2362@gmail.com"));

        assertThat(user, is(notNullValue()));

    }

    @Test
    public void testUpdateUserName() throws Exception {
        ResCommon user = null;
        String oldName = sideMenu.user.name;
        try {
            user = profileApiClient.updateMemberName(sideMenu.user.id, new ReqProfileName("test " + oldName));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(user, is(notNullValue()));

        ResLeftSideMenu sideMenu1 = getSideMenu();

        assertThat(sideMenu1.user.name, is(equalTo("test " + oldName)));

        profileApiClient.updateMemberName(sideMenu.user.id, new ReqProfileName(oldName));

    }
}