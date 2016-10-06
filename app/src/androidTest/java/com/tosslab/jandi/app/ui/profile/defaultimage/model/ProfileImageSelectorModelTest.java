package com.tosslab.jandi.app.ui.profile.defaultimage.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by tee on 16. 1. 6..
 */

@RunWith(AndroidJUnit4.class)
public class ProfileImageSelectorModelTest {

    ProfileImageSelectorModel profileImageSelectorModel;

    @Before
    public void setUp() throws Exception {
        profileImageSelectorModel =
                ProfileImageSelectorModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testGetCharacters() throws Exception {
        Assert.assertTrue(profileImageSelectorModel.getCharactersInfo().size() > 0);
    }

    @Test
    public void testGetColors() throws Exception {
        Assert.assertTrue(profileImageSelectorModel.getColors().size() > 0);
    }

}