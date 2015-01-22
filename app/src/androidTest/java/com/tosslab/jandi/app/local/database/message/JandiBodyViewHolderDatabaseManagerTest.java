package com.tosslab.jandi.app.local.database.message;

import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.lists.messages.MessageItemConverter;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiBodyViewHolderDatabaseManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
    }

    @Test
    public void testUpsertMessage() throws Exception {

        JandiRestClient jandiRestClient = new JandiRestClient_(Robolectric.application);
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).upsertAccountTeams(accountInfo.getMemberships());
        int teamId = accountInfo.getMemberships().get(0).getTeamId();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(teamId);

        ResLeftSideMenu infosForSideMenu = jandiRestClient.getInfosForSideMenu(teamId);

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(Robolectric.application);
        int entityId = infosForSideMenu.joinEntities.get(0).id;
        messageManipulator.initEntity(JandiConstants.TYPE_PUBLIC_TOPIC, entityId);
        ResMessages messages = messageManipulator.getMessages(-1);

        MessageItemConverter messageItemConverter = new MessageItemConverter();
        messageItemConverter.insertMessageItem(messages);
        List<MessageItem> messageItems = messageItemConverter.reformatMessages();

        int count = messageItems.size();

        int lastSaveIndex = Math.max(0, count - 20);

        List<ResMessages.Link> links = new ArrayList<ResMessages.Link>();

        for (int idx = count - 1; idx >= lastSaveIndex; --idx) {
            MessageItem item = messageItems.get(idx);

            links.add(item.getLink());
        }

        JandiMessageDatabaseManager.getInstance(Robolectric.application).upsertMessage(teamId, entityId, links);

        JandiDatabaseOpenHelper jandiDatabaseOpenHelper = new JandiDatabaseOpenHelper(Robolectric.application);

        Log.d("INFO", jandiDatabaseOpenHelper.getReadableDatabase().getPath());

        List<ResMessages.Link> savedMessages = JandiMessageDatabaseManager.getInstance(Robolectric.application).getSavedMessages(teamId, entityId);

        assertNotNull(savedMessages);

    }
}