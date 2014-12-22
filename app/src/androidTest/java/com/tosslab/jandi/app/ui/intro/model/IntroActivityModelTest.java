package com.tosslab.jandi.app.ui.intro.model;

import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class IntroActivityModelTest {

    private boolean isCalled;
    private IntroActivityModel introActivityModel;

    @Before
    public void setUp() throws Exception {


        IntroActivity introActivity_ = Robolectric.buildActivity(IntroActivity_.class).get();

        introActivityModel = IntroActivityModel_.getInstance_(introActivity_);

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    @Test
    public void testCheckNewVersion() throws Exception {

        // When : Check app version to server
        introActivityModel.checkNewVersion();

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isCalled;
            }
        });

        // Then : Success. if not called Fail

    }

    @Test
    public void testRetrieveThisAppVersion() throws Exception {

        // When : Get app version
        int versionCode = introActivityModel.retrieveThisAppVersion(Robolectric.application);

        // Then : Maybe....show update dialog..Because AndroidManifest.xml is not defined.
        assertThat(versionCode > 0, is(true));

    }

    @Test
    public void testGetLatestVersionInBackground() throws Exception {

        // When : Get App Version from server
        final boolean[] isCalled = new boolean[1];
        final int[] latestVersionInBackground = new int[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latestVersionInBackground[0] = introActivityModel.getLatestVersionInBackground();
                } catch (JandiNetworkException e) {
                    e.printStackTrace();
                }
                isCalled[0] = true;
            }
        }).start();

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isCalled[0];
            }
        });

        // Then : {App version from server} > 0
        assertThat(latestVersionInBackground[0] > 0, is(true));
    }

}