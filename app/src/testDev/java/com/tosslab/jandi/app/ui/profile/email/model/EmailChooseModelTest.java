package com.tosslab.jandi.app.ui.profile.email.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import retrofit.RetrofitError;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 15. 8. 31..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class EmailChooseModelTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.httpOn();

        JandiApplication.setContext(RuntimeEnvironment.application);
        JandiPreference.setAccessTokenType(RuntimeEnvironment.application, "bearer");
        JandiPreference.setAccessToken(RuntimeEnvironment.application, "413adaa8-7717-406a-abf0-536e947b7d8c");
    }

    @Test
    public void testRequestNewEmail() throws Exception {
        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = EmailChooseModel_.getInstance_(RuntimeEnvironment.application).requestNewEmail("aiden.jo@tosslab.com");
            System.out.println(resAccountInfo.toString());
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();

            try {
                ExceptionData exceptionData = (ExceptionData) retrofitError.getBodyAs(ExceptionData.class);
                System.out.println(exceptionData.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        assertNull(resAccountInfo);
    }
}