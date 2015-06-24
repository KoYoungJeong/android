package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);

        return infosForSideMenu;
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

        RequestApiManager.getInstance().updateMemberProfileByProfileApi(sideMenu.user.id, reqUpdateProfile);

        ResLeftSideMenu sideMenu1 = getSideMenu();

        assertThat(sideMenu1.user.u_extraData.phoneNumber, is(equalTo("test " + oldPhoneNumber)));
        assertThat(sideMenu1.user.u_extraData.position, is(equalTo("test " + oldPosition)));
        assertThat(sideMenu1.user.u_extraData.department, is(equalTo("test " + oldDepartment)));
        assertThat(sideMenu1.user.u_statusMessage, is(equalTo("test " + oldStatusMessage)));

        reqUpdateProfile.department = oldDepartment;
        reqUpdateProfile.position = oldPosition;
        reqUpdateProfile.statusMessage = oldStatusMessage;
        reqUpdateProfile.phoneNumber = oldPhoneNumber;

        RequestApiManager.getInstance().updateMemberProfileByProfileApi(sideMenu.user.id, reqUpdateProfile);


    }

    @Ignore
    @Test
    public void testUpdateUserEmail() throws Exception {
        ResLeftSideMenu.User user = RequestApiManager.getInstance().updateMemberEmailByProfileApi(sideMenu.user.id, new ReqAccountEmail(JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserEmails().get(0).getId()));
        assertThat(user, is(notNullValue()));
    }

    @Test
    public void testUpdateUserName() throws Exception {
        ResCommon user = null;
        String oldName = sideMenu.user.name;
        try {
            user = RequestApiManager.getInstance().updateMemberNameByProfileApi(sideMenu.user.id, new ReqProfileName("test " + oldName));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(user, is(notNullValue()));

        ResLeftSideMenu sideMenu1 = getSideMenu();

        assertThat(sideMenu1.user.name, is(equalTo("test " + oldName)));

        RequestApiManager.getInstance().updateMemberNameByProfileApi(sideMenu.user.id, new ReqProfileName(oldName));

    }
}