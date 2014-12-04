package com.tosslab.jandi.app;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by Steve SeongUg Jung on 14. 12. 1..
 */
@Config(manifest = "app/src/main/AndroidManifest.xml",emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class JandiApplicationTest {

    @Test
    public void init() {

        Assert.assertThat(Robolectric.application, is(notNullValue()));
    }
}
