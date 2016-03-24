package com.tosslab.jandi.app.ui.profile.email.model;

import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;



import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by tonyjs on 15. 8. 31..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class EmailChooseModelTest {

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(RuntimeEnvironment.application);
    }

    @Test
    public void testRequestNewEmail() throws Exception {
        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = EmailChooseModel_.getInstance_(RuntimeEnvironment.application).requestNewEmail("aiden.jo@tosslab.com");
            fail("it never occured : Response :" + resAccountInfo.toString());
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
            assertThat(retrofitError.getResponse().getStatus(), is(equalTo(400)));
            try {
                ExceptionData exceptionData = (ExceptionData) retrofitError.getBodyAs(ExceptionData.class);
                assertThat(exceptionData.getCode(), is(equalTo(40001)));
            } catch (Exception e) {
                e.printStackTrace();
                fail("wrong response");
            }
        }
    }
}