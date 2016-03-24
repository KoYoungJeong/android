package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ResEventHistory;

/**
 * Created by tee on 15. 11. 19..
 */
public interface IEventsApiLoader {
    Executor<ResEventHistory> loadGetEventHistory(long ts, long memberId, String eventType, Integer size);
}
