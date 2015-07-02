package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.network.models.ResRoomInfo;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IRoomsApiAuth {

    ResRoomInfo getRoomInfoByRoomsApi(int teamId, int roomId) throws RetrofitError;

}
