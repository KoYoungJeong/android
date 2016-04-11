package com.tosslab.jandi.app.ui.profile.modify.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ModifyProfileModelTest {

    private ModifyProfileModel modifyProfileModel;
    private ResLeftSideMenu.User user;

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
        modifyProfileModel = ModifyProfileModel_.getInstance_(JandiApplication.getContext());
        user = EntityManager.getInstance().getMe().getUser();
    }

    @Test
    public void testGetProfile() throws Exception {
        ResLeftSideMenu.User profile = modifyProfileModel.getProfile();

        assertThat(user.u_email, is(equalTo(profile.u_email)));
        assertThat(user.u_extraData.department, is(equalTo(profile.u_extraData.department)));
        assertThat(user.u_extraData.phoneNumber, is(equalTo(profile.u_extraData.phoneNumber)));
        assertThat(user.u_extraData.position, is(equalTo(profile.u_extraData.position)));
        assertThat(user.name, is(equalTo(profile.name)));
        assertThat(user.u_photoThumbnailUrl.largeThumbnailUrl, is(equalTo(profile.u_photoThumbnailUrl.largeThumbnailUrl)));
    }

    @Test
    public void testUpdateProfile() throws Exception {

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.department = "department : " + new Date().toString();

            // When
            ResLeftSideMenu.User newProfile = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.u_extraData.department, is(not(equalTo(reqUpdateProfile.department))));
            assertThat(newProfile.u_extraData.department, is(equalTo(reqUpdateProfile.department)));

        }

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.phoneNumber = "010-1234-5678";

            // When
            ResLeftSideMenu.User newProfile = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.u_extraData.phoneNumber, is(not(equalTo(reqUpdateProfile.phoneNumber))));
            assertThat(newProfile.u_extraData.phoneNumber, is(equalTo(reqUpdateProfile.phoneNumber)));

        }

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.position = "position  : " + new Date().toString();

            // When
            ResLeftSideMenu.User newProfile = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.u_extraData.position, is(not(equalTo(reqUpdateProfile.position))));
            assertThat(newProfile.u_extraData.position, is(equalTo(reqUpdateProfile.position)));

        }

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.statusMessage = "statusMessage  : " + new Date().toString();

            // When
            ResLeftSideMenu.User newProfile = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.u_statusMessage, is(not(equalTo(reqUpdateProfile.statusMessage))));
            assertThat(newProfile.u_statusMessage, is(equalTo(reqUpdateProfile.statusMessage)));

        }


        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.statusMessage = user.u_statusMessage;
        reqUpdateProfile.position = user.u_extraData.position;
        reqUpdateProfile.department = user.u_extraData.department;
        reqUpdateProfile.phoneNumber = user.u_extraData.phoneNumber;

        modifyProfileModel.updateProfile(reqUpdateProfile);

    }

    @Test
    public void testUpdateProfileName() throws Exception {
        String newName = new Date().toString().replaceAll(" ", "");
        modifyProfileModel.updateProfileName(new ReqProfileName(newName));

        ResLeftSideMenu.User newProfile = modifyProfileModel.getProfile();
        assertThat(newProfile.name, is(equalTo(newName)));
        assertThat(newProfile.name, is(not(equalTo(user.name))));

        modifyProfileModel.updateProfileName(new ReqProfileName(user.name));
    }

    @Test
    public void testGetAccountEmails() throws Exception {
        String[] accountEmails = modifyProfileModel.getAccountEmails();

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        Observable.from(AccountRepository.getRepository().getAccountEmails())
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .map(userEmail1 -> userEmail1.getId())
                .subscribe(subscriber);

        subscriber.assertValueCount(accountEmails.length);
        subscriber.assertValues(accountEmails);
    }

    @Test
    public void testUpdateProfileEmail() throws Exception {
        try {
            String email = "fail@fail.com";
            modifyProfileModel.updateProfileEmail(email);
            fail("성공할 수 없는 이메일인데.. : " + email);
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    @Test
    public void testIsMyId() throws Exception {
        boolean isMyId = modifyProfileModel.isMyId(EntityManager.getInstance().getMe().getId());
        assertThat(isMyId, is(true));
        isMyId = modifyProfileModel.isMyId(-1);
        assertThat(isMyId, is(false));
    }

    @Test
    public void testGetSavedProfile() throws Exception {
        ResLeftSideMenu.User savedProfile = modifyProfileModel.getSavedProfile();
        assertThat(savedProfile.u_email, is(equalTo(user.u_email)));
        assertThat(savedProfile.u_extraData.department, is(equalTo(user.u_extraData.department)));
        assertThat(savedProfile.u_extraData.phoneNumber, is(equalTo(user.u_extraData.phoneNumber)));
        assertThat(savedProfile.u_extraData.position, is(equalTo(user.u_extraData.position)));
        assertThat(savedProfile.name, is(equalTo(user.name)));
        assertThat(savedProfile.u_photoThumbnailUrl.largeThumbnailUrl, is(equalTo(user.u_photoThumbnailUrl.largeThumbnailUrl)));
    }
}