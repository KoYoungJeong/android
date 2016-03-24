package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IRoomsApiLoader {

    Executor<ResRoomInfo> loadGetRoomInfoByRoomsApi(long teamId, long roomId);

}
