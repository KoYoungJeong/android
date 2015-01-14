package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.app.Application;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.TokenUtil;

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

        JandiRestClient jandiRestClient = new JandiRestClient_(application);
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(application));
        ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();
        JandiAccountDatabaseManager.getInstance(application).upsertAccountTeams(accountInfo.getMemberships());
        JandiAccountDatabaseManager.getInstance(application).updateSelectedTeam(accountInfo.getMemberships().get(0).getTeamId());
    }

    @Test
    public void testConvertChatItem() throws Exception {


        JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(application);
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
        JandiEntityDatabaseManager.getInstance(application).upsertLeftSideMenu(totalEntitiesInfo);

        MainChatListModel mainChatListModel = MainChatListModel_.getInstance_(application);

        List<ResChat> chatList = mainChatListModel.getChatList(totalEntitiesInfo.user.id);

        List<ChatItem> chatItems = mainChatListModel.convertChatItem(JandiAccountDatabaseManager.getInstance(application).getSelectedTeamInfo().getTeamId(), chatList);

        assertThat(chatItems.size(), is(equalTo(chatList.size())));
    }
}