package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.network.models.ResRoomInfo;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IRoomsApiAuth {

    ResRoomInfo getRoomInfoByRoomsApi(long teamId, long roomId) throws IOException;

}
