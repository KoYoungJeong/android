package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.network.models.ResEventHistory;

/**
 * Created by tee on 15. 11. 19..
 */
public interface IEventsApiAuth {
    ResEventHistory getEventHistory(long ts, Integer memberId, String eventType, int size);
}
