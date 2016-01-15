package com.tosslab.jandi.app.ui.search.messages.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.junit.Assert.assertNotNull;

/**
 * Created by tonyjs on 15. 12. 29..
 */
@RunWith(AndroidJUnit4.class)
public class MessageSearchModelTest {

    MessageSearchModel searchModel;

    @Before
    public void setup() throws Exception {
        BaseInitUtil.initData();
        searchModel = MessageSearchModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testRequestSearchQuery() throws Exception {
        // Given
        final String query = "가";
        int teamId = EntityManager.getInstance().getTeamId();

        // When
        ResMessageSearch searchQuery = searchModel.requestSearchQuery(teamId, query, 1, 20, -1, -1);

        // Then
        assertNotNull(searchQuery);
    }
}