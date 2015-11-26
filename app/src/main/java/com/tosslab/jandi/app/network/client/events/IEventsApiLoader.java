package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResEventHistory;

/**
 * Created by tee on 15. 11. 19..
 */
public interface IEventsApiLoader {
    IExecutor<ResEventHistory> loadGetEventHistory(long ts, Integer memberId, String eventType, Integer size);
}
