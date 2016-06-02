package com.tosslab.jandi.app.ui.profile.modify.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

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
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ModifyProfileModelTest {

    private ModifyProfileModel modifyProfileModel;
    private User user;

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
        user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
    }

    @Test
    public void testGetProfile() throws Exception {
        User profile = modifyProfileModel.getProfile();

        assertThat(user.getEmail(), is(equalTo(profile.getEmail())));
        assertThat(user.getDivision(), is(equalTo(profile.getDivision())));
        assertThat(user.getPhoneNumber(), is(equalTo(profile.getPhoneNumber())));
        assertThat(user.getPosition(), is(equalTo(profile.getPosition())));
        assertThat(user.getName(), is(equalTo(profile.getName())));
        assertThat(user.getPhotoUrl(), is(equalTo(profile.getPhotoUrl())));
    }

    @Test
    public void testUpdateProfile() throws Exception {

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.department = "department : " + new Date().toString();

            // When
            ResCommon resCommon = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.getDivision(), is(not(equalTo(reqUpdateProfile.department))));
            assertThat(resCommon, is(notNullValue()));

        }

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.phoneNumber = "010-1234-5678";

            // When
            ResCommon resCommon = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.getPhoneNumber(), is(not(equalTo(reqUpdateProfile.phoneNumber))));
            assertThat(resCommon, is(notNullValue()));


        }

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.position = "position  : " + new Date().toString();

            // When
            ResCommon resCommon = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.getPosition(), is(not(equalTo(reqUpdateProfile.position))));
            assertThat(resCommon, is(notNullValue()));


        }

        {
            // Given
            ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
            reqUpdateProfile.statusMessage = "statusMessage  : " + new Date().toString();

            // When
            ResCommon resCommon = modifyProfileModel.updateProfile(reqUpdateProfile);

            //Then
            assertThat(user.getStatusMessage(), is(not(equalTo(reqUpdateProfile.statusMessage))));
            assertThat(resCommon, is(notNullValue()));

        }


        ReqUpdateProfile reqUpdateProfile = new ReqUpdateProfile();
        reqUpdateProfile.statusMessage = user.getStatusMessage();
        reqUpdateProfile.position = user.getPosition();
        reqUpdateProfile.department = user.getDivision();
        reqUpdateProfile.phoneNumber = user.getPhoneNumber();

        modifyProfileModel.updateProfile(reqUpdateProfile);

    }

    @Test
    public void testUpdateProfileName() throws Exception {
        String newName = new Date().toString().replaceAll(" ", "");
        modifyProfileModel.updateProfileName(new ReqProfileName(newName));

        User newProfile = modifyProfileModel.getProfile();
        assertThat(newProfile.getName(), is(equalTo(newName)));
        assertThat(newProfile.getName(), is(not(equalTo(user.getName()))));

        modifyProfileModel.updateProfileName(new ReqProfileName(user.getName()));
    }

    @Test
    public void testGetAccountEmails() throws Exception {
        String[] accountEmails = modifyProfileModel.getAccountEmails();

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        Observable.from(AccountRepository.getRepository().getAccountEmails())
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .map(ResAccountInfo.UserEmail::getId)
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
        boolean isMyId = modifyProfileModel.isMyId(TeamInfoLoader.getInstance().getMyId());
        assertThat(isMyId, is(true));
        isMyId = modifyProfileModel.isMyId(-1);
        assertThat(isMyId, is(false));
    }

    @Test
    public void testGetSavedProfile() throws Exception {
        User savedProfile = modifyProfileModel.getSavedProfile();
        assertThat(savedProfile.getEmail(), is(equalTo(user.getEmail())));
        assertThat(savedProfile.getDivision(), is(equalTo(user.getDivision())));
        assertThat(savedProfile.getPhoneNumber(), is(equalTo(user.getPhoneNumber())));
        assertThat(savedProfile.getPosition(), is(equalTo(user.getPosition())));
        assertThat(savedProfile.getName(), is(equalTo(user.getName())));
        assertThat(savedProfile.getPhotoUrl(), is(equalTo(user.getPhotoUrl())));
    }
}