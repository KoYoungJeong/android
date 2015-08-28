package com.tosslab.jandi.app.services.socket;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 7. 13..
 */
public class EntitySocketModel {

    private final PublishSubject<EntityRefreshEventWrapper> refreshEntityPublishSubject;
    private final Subscription refreshEntitySubscribe;
    private final Context context;

    public EntitySocketModel(Context context) {
        this.context = context;
        refreshEntityPublishSubject = PublishSubject.create();
        refreshEntitySubscribe = refreshEntityPublishSubject
                .observeOn(Schedulers.io())
                .onBackpressureBuffer()
                .buffer(500, TimeUnit.MILLISECONDS)
                .filter(entityRefreshEventWrappers -> !entityRefreshEventWrappers.isEmpty())
                .subscribe(eventWrappers -> handleEvent(EntitySocketModel.this.context,
                        eventWrappers)
                        , throwable -> LogUtil.e("Socket RefreshEntity Error", throwable));
    }

    public void refreshEntity(EntityRefreshEventWrapper eventWrapper) {
        if (!refreshEntitySubscribe.isUnsubscribed()) {
            refreshEntityPublishSubject.onNext(eventWrapper);
        }
    }

    public void stopObserver() {
        if (refreshEntitySubscribe != null && !refreshEntitySubscribe.isUnsubscribed()) {
            refreshEntitySubscribe.unsubscribe();
        }
    }

    private void handleEvent(Context context, List<EntityRefreshEventWrapper> eventWrappers) {
        try {
            EntityClientManager jandiEntityClient = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu resLeftSideMenu = jandiEntityClient.getTotalEntitiesInfo();

            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(resLeftSideMenu);

            setBadgeCount(context, resLeftSideMenu);

            EntityManager.getInstance().refreshEntity();

            Observable.from(eventWrappers)
                    .takeFirst(eventWrapper -> eventWrapper.postRetrieveEvent)
                    .subscribe(eventWrapper -> postRetrieveTopicEvent());

            Observable.from(eventWrappers)
                    .takeFirst(eventWrapper ->
                            !TextUtils.isEmpty(eventWrapper.socketMessageEventContent))
                    .subscribe(eventWrapper -> postSocketMessageEvent
                            (eventWrapper.socketMessageEventContent));

            Observable.from(eventWrappers)
                    .filter(eventWrapper -> eventWrapper.event != null)
                    .subscribe(eventWrapper -> postHandledEvent(eventWrapper.event));


        } catch (RetrofitError e) {
        }
    }

    private void postHandledEvent(Object event) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(event.getClass())) {
            eventBus.post(event);
        }
    }

    private void setBadgeCount(Context context, ResLeftSideMenu resLeftSideMenu) {
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
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

    static class EntityRefreshEventWrapper {
        boolean postRetrieveEvent;
        boolean parseUpdate;
        String socketMessageEventContent;
        Object event;

        public EntityRefreshEventWrapper(boolean postRetrieveEvent, boolean parseUpdate, String
                socketMessageEventContent, Object event) {
            this.postRetrieveEvent = postRetrieveEvent;
            this.parseUpdate = parseUpdate;
            this.socketMessageEventContent = socketMessageEventContent;
            this.event = event;
        }
    }
}
