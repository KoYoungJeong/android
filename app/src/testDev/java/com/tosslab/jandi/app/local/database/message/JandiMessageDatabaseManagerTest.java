package com.tosslab.jandi.app.local.database.message;

import android.util.Patterns;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiMessageDatabaseManagerTest {

    public static final int TEAM_ID = 1;
    public static final int ENTITY_ID = 1;

    @Test
    public void testInsertSendMessage() throws Exception {

        System.out.println(Patterns.WEB_URL);

        JandiMessageDatabaseManager databaseManager = JandiMessageDatabaseManager.getInstance(Robolectric.application);
        long test = databaseManager.insertSendMessage(TEAM_ID, ENTITY_ID, "test");

        assertThat(test, is(greaterThan(0l)));

        List<ResMessages.Link> sendMessage = databaseManager.getSendMessage(TEAM_ID, ENTITY_ID);

        boolean find = false;
        for (ResMessages.Link link : sendMessage) {
            if (((DummyMessageLink) link).getLocalId() == test) {
                find = true;
            }
        }

        assertThat(find, is(true));
    }

    @Test
    public void testDeleteSendMessage() throws Exception {

        JandiMessageDatabaseManager databaseManager = JandiMessageDatabaseManager.getInstance(Robolectric.application);
        long test = databaseManager.insertSendMessage(TEAM_ID, ENTITY_ID, "test");

        databaseManager.deleteSendMessage(test);
        List<ResMessages.Link> sendMessage = databaseManager.getSendMessage(TEAM_ID, ENTITY_ID);

        boolean find = false;
        for (ResMessages.Link link : sendMessage) {
            if (((DummyMessageLink) link).getLocalId() == test) {
                find = true;
            }
        }

        assertThat(find, is(false));
    }
}