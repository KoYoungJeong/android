package com.tosslab.jandi.app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 15. 6. 9..
 */
public class BadgeHandleService extends IntentService {
    public static final String TAG = BadgeHandleService.class.getSimpleName();
    public static final String KEY_POST_RETRIEVE_TOPIC_EVENT = "post_retrieve_topic_event";
    public static final String KEY_SOCKET_MESSAGE_EVENT = "socket_message_event";

    public BadgeHandleService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Context context = getApplicationContext();
            JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);
            ResLeftSideMenu resLeftSideMenu = jandiEntityClient.getTotalEntitiesInfo();

            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(resLeftSideMenu);

            setBadgeCount(context, resLeftSideMenu);

            EntityManager.getInstance(context).refreshEntity(resLeftSideMenu);

            if (intent == null) {
                return;
            }

            boolean postRetrieveTopicEvent =
                    intent.getBooleanExtra(KEY_POST_RETRIEVE_TOPIC_EVENT, false);
            if (postRetrieveTopicEvent) {
                postRetrieveTopicEvent();
            }

            String socketMessageEventContent = intent.getStringExtra(KEY_SOCKET_MESSAGE_EVENT);
            if (!TextUtils.isEmpty(socketMessageEventContent)) {
                postSocketMessageEvent(socketMessageEventContent);
            }

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    private void setBadgeCount(Context context, ResLeftSideMenu resLeftSideMenu) {
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
        LogUtil.e(TAG, "totalUnreadCount - " + totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
    }

    private void postRetrieveTopicEvent() {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(RetrieveTopicListEvent.class)) {
            eventBus.post(new RetrieveTopicListEvent());
        }
    }

    private void postSocketMessageEvent(String socketMessageEventContent) {
        try {
            SocketMessageEvent socketMessageEvent = JacksonMapper.getInstance().getObjectMapper()
                    .readValue(socketMessageEventContent, SocketMessageEvent.class);
            EventBus eventBus = EventBus.getDefault();
            if (eventBus.hasSubscriberForEvent(SocketMessageEvent.class)) {
                eventBus.post(socketMessageEvent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
