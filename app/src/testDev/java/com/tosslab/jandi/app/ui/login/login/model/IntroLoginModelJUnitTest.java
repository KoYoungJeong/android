package com.tosslab.jandi.app.ui.login.login.model;

import android.content.Context;
import android.os.Looper;
import android.test.UiThreadTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tee on 15. 10. 29..
 */

@RunWith(MockitoJUnitRunner.class)
public class IntroLoginModelJUnitTest {

    @Mock
    Context mContext;

    private IntroLoginModel introLoginModel;

    @Before
    public void setUp() throws Exception {
        introLoginModel = IntroLoginModel_.getInstance_(mContext);
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