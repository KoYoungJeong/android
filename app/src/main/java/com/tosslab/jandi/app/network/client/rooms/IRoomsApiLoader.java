package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IRoomsApiLoader {

    IExecutor loadGetRoomInfoByRoomsApi(int teamId, int roomId);

}
