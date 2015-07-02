package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IRoomsApiLoader {

    IExecutor<ResRoomInfo> loadGetRoomInfoByRoomsApi(int teamId, int roomId);

}
