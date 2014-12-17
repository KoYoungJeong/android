package com.tosslab.jandi.app.ui.login.login.model;

import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.ui.login.IntroMainActivity;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class IntroLoginModelTest {


    private IntroMainActivity introMainActivity;
    private IntroLoginModel introLoginModel;

    @Before
    public void setUp() throws Exception {
        introMainActivity = Robolectric.buildActivity(IntroMainActivity_.class).get();
        introLoginModel = IntroLoginModel_.getInstance_(introMainActivity);

        // Real Connect Dev Server
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    @Test
    public void testCreateTeamInBackground() throws Exception {


        final boolean[] success = new boolean[1];
        final boolean[] isCalled = new boolean[1];
        introLoginModel.setCallback(new IntroLoginModel.Callback() {
            @Override
            public void onCreateTeamSuccess() {
                isCalled[0] = true;
                success[0] = true;
                System.out.println("Success :::: onCreateTeamSuccess ::::");
            }

            @Override
            public void onCreateTeamFail(int stringResId) {
                isCalled[0] = true;
                System.out.println("Success :::: onCreateTeamFail ::::");
            }

            @Override
            public void onLoginSuccess(String email) {

            }

            @Override
            public void onLoginFail(int errorStringResId) {

            }
        });

        introLoginModel.createTeamInBackground("steve@tosslab.com");

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isCalled[0];
            }
        });

        assertThat(success[0], is(true));
    }

    @Test
    public void testGetTeamListInBackground() throws Exception {

        final ResMyTeam[] resMyTeams = new ResMyTeam[1];
        final boolean[] isCalled = new boolean[1];
        introLoginModel.setCallback(new IntroLoginModel.Callback() {
            @Override
            public void onCreateTeamSuccess() {

            }

            @Override
            public void onCreateTeamFail(int stringResId) {

            }

            @Override
            public void onLoginSuccess(String email) {
                resMyTeams[0] = new ResMyTeam();
                isCalled[0] = true;
            }

            @Override
            public void onLoginFail(int errorStringResId) {
                isCalled[0] = true;
            }
        });

        introLoginModel.startLogin("steve@tosslab.com", "1234");

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isCalled[0];
            }
        });

        assertThat(JandiPreference.getAccessToken(Robolectric.application), is(notNullValue()));
    }

    @Test
    public void testIsValidEmailFormat() throws Exception {

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@tosslab.com");

            assertThat(validEmailFormat, is(equalTo(true)));
        }

        {

            boolean validEmailFormat = introLoginModel.isValidEmailFormat("123@a.com");
            assertThat(validEmailFormat, is(equalTo(true)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@.com");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@tosslab.");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("steve@tosslab");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

        {
            boolean validEmailFormat = introLoginModel.isValidEmailFormat("@a.com");

            assertThat(validEmailFormat, is(equalTo(false)));
        }

    }
}