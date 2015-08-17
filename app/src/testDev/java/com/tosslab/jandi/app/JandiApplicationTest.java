package com.tosslab.jandi.app;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by Steve SeongUg Jung on 14. 12. 1..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class JandiApplicationTest {

    @Test
    public void init() {

        Assert.assertThat(RuntimeEnvironment.application, is(notNullValue()));
    }

}
