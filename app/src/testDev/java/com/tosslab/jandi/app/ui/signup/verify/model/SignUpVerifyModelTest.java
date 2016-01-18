package com.tosslab.jandi.app.ui.signup.verify.model;

import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.signup.verify.SignUpVerifyActivity_;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.shadows.httpclient.FakeHttp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by tonyjs on 15. 6. 2..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class SignUpVerifyModelTest {

    private SignUpVerifyModel signUpVerifyModel;

    @Before
    public void setUp() throws Exception {
        SignUpVerifyActivity_ signUpVerifyActivity_ =
                Robolectric.buildActivity(SignUpVerifyActivity_.class).get();
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        signUpVerifyModel = SignUpVerifyModel_.getInstance_(signUpVerifyActivity_);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    @Test
    public void testIsValidVerificationCode() throws Exception {
        boolean validVerificationCode = signUpVerifyModel.isValidVerificationCode("1234");

        assertThat(validVerificationCode, is(true));
    }

    @Test
    public void testRequestNewVerificationCode() throws Exception {
        ResCommon resCommon = signUpVerifyModel.requestNewVerificationCode("gree@gree.com");

        assertNotNull(resCommon);
    }
}