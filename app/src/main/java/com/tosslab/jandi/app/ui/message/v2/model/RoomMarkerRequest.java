package com.tosslab.jandi.app.ui.message.v2.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.rooms.RoomsApiClient;
import com.tosslab.jandi.app.network.client.rooms.RoomsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class RoomMarkerRequest implements Request<ResRoomInfo> {

    private final Context context;
    private final int teamId;
    private final int roomId;
    private final RoomsApiClient roomsApiClient;

    private RoomMarkerRequest(Context context, int teamId, int roomId, RoomsApiClient roomsApiClient) {
        this.context = context;
        this.teamId = teamId;
        this.roomId = roomId;
        this.roomsApiClient = roomsApiClient;
    }

    public static RoomMarkerRequest create(Context context, int teamId, int roomId) {
        return new RoomMarkerRequest(context, teamId, roomId, new RoomsApiClient_(context));
    }

    @Override
    public ResRoomInfo request() throws JandiNetworkException {
        roomsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        return roomsApiClient.getRoomInfo(teamId, roomId);
    }
}