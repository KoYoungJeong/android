package com.tosslab.jandi.app.ui.maintab.more.model;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import setup.BaseInitUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MainMoreModelTest {

    private MainMoreModel mainMoreModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        mainMoreModel = MainMoreModel_.getInstance_(JandiApplication.getContext());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testGetVersionName() throws Exception {
        String versionName = mainMoreModel.getVersionName();

        assertThat(versionName, is(notNullValue()));
        assertThat(versionName.length(), is(greaterThan(0)));
    }

    @Test
    public void testGetOtherTeamBadge() throws Exception {

        int otherTeamBadge = mainMoreModel.getOtherTeamBadge();
        assertThat(otherTeamBadge, is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetEnabledUserCount() throws Exception {
        int enabledUserCount = mainMoreModel.getEnabledUserCount();
        assertThat(enabledUserCount, is(greaterThanOrEqualTo(0)));

    }

    @Test
    public void testIsConnectedNetwork() throws Exception {
        boolean connectedNetwork = mainMoreModel.isConnectedNetwork();
        assertThat(connectedNetwork, is(true));

        BaseInitUtil.disconnectWifi();

        connectedNetwork = mainMoreModel.isConnectedNetwork();
        assertThat(connectedNetwork, is(false));

        BaseInitUtil.restoreContext();
    }

    @Test
    public void testNeedToUpdate() throws Exception {
        Pair<Boolean, Integer> pair = mainMoreModel.needToUpdate();
        assertThat(pair, is(notNullValue()));

        if (pair.second == -1) {
            assertThat(pair.first, is(false));
        }
    }

    @Test
    public void testGetSupportUrlEachLanguage() throws Exception {
        String segmentOfEnd = getSupportUrlLangCode();

        String url = mainMoreModel.getSupportUrlEachLanguage();
        assertThat(url, is(startsWith("https://jandi.zendesk.com/hc")));
        assertThat(url, is(endsWith(segmentOfEnd)));

    }

    @Test
    public void testIsIn3Seconds() throws Exception {
        boolean in3Seconds = mainMoreModel.isIn3Seconds(System.currentTimeMillis() - 1);
        assertThat(in3Seconds, is(true));
        in3Seconds = mainMoreModel.isIn3Seconds(System.currentTimeMillis() - 3001);
        assertThat(in3Seconds, is(false));
    }

    @Test
    public void testGetUserInfosForBugReport() throws Exception {
        List<Pair<String, String>> userInfosForBugReport = mainMoreModel.getUserInfosForBugReport();
        assertThat(userInfosForBugReport.size(), is(greaterThan(0)));
    }

    @Test
    public void testGetUserInfoSpans() throws Exception {
        ArrayList<Pair<String, String>> userInfos = new ArrayList<>();
        userInfos.add(new Pair<>("first-a", "second-a"));
        SpannableStringBuilder userInfoSpans = mainMoreModel.getUserInfoSpans(userInfos);

        assertThat(userInfoSpans.toString(), is(equalTo("first-a\nsecond-a")));
    }

    @NonNull
    private String getSupportUrlLangCode() {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
        String segmentOfEnd;
        switch (locale.getLanguage()) {
            case "ko":
                segmentOfEnd = "ko";
                break;
            case "zh":
                segmentOfEnd = "zh-" + locale.getCountry();
                break;
            case "ja":
            default:
                segmentOfEnd = "en-us";
                break;
        }
        return segmentOfEnd;
    }
}