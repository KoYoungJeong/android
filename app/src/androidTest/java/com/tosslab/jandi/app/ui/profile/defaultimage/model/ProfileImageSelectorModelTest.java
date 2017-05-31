package com.tosslab.jandi.app.ui.profile.defaultimage.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;

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
        profileImageSelectorModel = new ProfileImageSelectorModel(() -> new ProfileApi(InnerApiRetrofitBuilder.getInstance()));

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