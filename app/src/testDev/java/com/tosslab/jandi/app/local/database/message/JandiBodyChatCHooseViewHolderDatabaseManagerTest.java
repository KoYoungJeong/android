package com.tosslab.jandi.app.local.database.message;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.lists.messages.MessageItemConverter;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiBodyChatCHooseViewHolderDatabaseManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(RuntimeEnvironment.application).getWritableDatabase().close();
    }


    @Test
    public void testUpsertMessage() throws Exception {

        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(RuntimeEnvironment.application);
        int entityId = infosForSideMenu.joinEntities.iterator().next().id;
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

        MessageRepository.getRepository().upsertMessages(links);

        List<ResMessages.Link> savedMessages = MessageRepository.getRepository().getMessages(entityId);

        assertNotNull(savedMessages);

    }
}