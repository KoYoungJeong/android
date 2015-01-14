package com.tosslab.jandi.app.network.client.chat;

import android.app.Application;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class ChatsApiClientTest {

    private Application context;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.application;
        BaseInitUtil.initData(context);
    }

    @Test
    public void testGetChatList() throws Exception {

        ChatsApiClient chatsApiClient = new ChatsApiClient_(context);
        chatsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        JandiRestClient jandiRestClient = new JandiRestClient_(context);
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();

        List<ResChat> chatList = chatsApiClient.getChatList(accountInfo.getMemberships().get(0).getMemberId());

        assertTrue(chatList.size() > 0);
    }
}