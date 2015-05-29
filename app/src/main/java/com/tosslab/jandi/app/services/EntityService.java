package com.tosslab.jandi.app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 15. 6. 9..
 */
public class EntityService extends IntentService {
    public static final String TAG = EntityService.class.getSimpleName();
    public static final String KEY_POST_EVENT = "post_event";

    public EntityService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Context context = getApplicationContext();
            JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);
            ResLeftSideMenu resLeftSideMenu = jandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(resLeftSideMenu);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
            Log.e(TAG, "totalUnreadCount - " + totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(resLeftSideMenu);

            boolean postEvent = intent != null && intent.getBooleanExtra(KEY_POST_EVENT, false);
            if (postEvent) {
                EventBus eventBus = EventBus.getDefault();
                if (eventBus.hasSubscriberForEvent(RetrieveTopicListEvent.class)) {
                    Log.i(TAG, "Event has subscribe.");
                    eventBus.post(new RetrieveTopicListEvent());
                }
            }
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }
}
