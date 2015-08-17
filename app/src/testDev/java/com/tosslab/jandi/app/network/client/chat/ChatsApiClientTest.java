package com.tosslab.jandi.app.network.client.chat;

import android.app.Application;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class ChatsApiClientTest {

    private Application context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        BaseInitUtil.initData(context);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }


    @Test
    public void testGetChatList() throws Exception {

//        ChatsApiClient chatsApiClient = new ChatsApiClient_(context);
//        chatsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//
//        JandiRestClient jandiRestClient = new JandiRestClient_(context);
//        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//        ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();
        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

//        List<ResChat> chatList = chatsApiClient.getChatList(accountInfo.getMemberships().get(0).getMemberId());
        List<ResChat> chatList = RequestApiManager.getInstance().getChatListByChatApi(accountInfo
                .getMemberships().iterator().next().getMemberId());
        assertTrue(chatList.size() > 0);
    }
}