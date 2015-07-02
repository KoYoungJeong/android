package com.tosslab.jandi.app.local.database.message;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.lists.messages.MessageItemConverter;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.After;
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
public class JandiBodyChatCHooseViewHolderDatabaseManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    @Test
    public void testUpsertMessage() throws Exception {

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).upsertAccountTeams(accountInfo.getMemberships());
        int teamId = accountInfo.getMemberships().get(0).getTeamId();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(teamId);

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(Robolectric.application);
        int entityId = infosForSideMenu.joinEntities.get(0).id;
        messageManipulator.initEntity(JandiConstants.TYPE_PUBLIC_TOPIC, entityId);
        ResMessages messages = messageManipulator.getMessages(-1, 20);

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

        List<ResMessages.Link> savedMessages = JandiMessageDatabaseManager.getInstance(Robolectric.application).getSavedMessages(teamId, entityId);

        assertNotNull(savedMessages);

    }
}