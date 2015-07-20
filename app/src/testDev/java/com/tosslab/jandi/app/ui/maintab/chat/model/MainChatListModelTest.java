package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.app.Application;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class MainChatListModelTest {
    Application application;

    @Before
    public void setUp() throws Exception {
        application = Robolectric.application;
        BaseInitUtil.initData(application);

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        AccountRepository.getRepository().updateSelectedTeamInfo(accountInfo.getMemberships().iterator().next().getTeamId());
    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    @Test
    public void testConvertChatItem() throws Exception {

        EntityClientManager entityClientManager = EntityClientManager_.getInstance_(application);
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
        JandiEntityDatabaseManager.getInstance(application).upsertLeftSideMenu(totalEntitiesInfo);

        MainChatListModel mainChatListModel = MainChatListModel_.getInstance_(application);

        List<ResChat> chatList = mainChatListModel.getChatList(totalEntitiesInfo.user.id);

        List<ChatItem> chatItems = mainChatListModel.convertChatItem(application,
                AccountRepository.getRepository().getSelectedTeamInfo().getTeamId(), chatList);

        assertThat(chatItems.size(), is(equalTo(chatList.size())));

    }
}