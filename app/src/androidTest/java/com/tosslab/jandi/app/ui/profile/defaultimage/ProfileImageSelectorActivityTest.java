package com.tosslab.jandi.app.ui.profile.defaultimage;

import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by tee on 16. 1. 6..
 */
public class ProfileImageSelectorActivityTest {

    @Rule
    public ActivityTestRule<ProfileImageSelectorActivity_> rule = new ActivityTestRule<>(ProfileImageSelectorActivity_.class, false, true);
    ProfileImageSelectorActivity profileImageSelectorActivity;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testButtons() {
        profileImageSelectorActivity = rule.getActivity();
        SystemClock.sleep(100000);
    }

}