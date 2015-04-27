package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;

import com.tosslab.jandi.app.local.database.rooms.marker.JandiMarkerDatabaseManager;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class UnreadCountUtil {

    public static int getUnreadCount(Context context, int teamId, int roomId, int linkId) {

        List<ResRoomInfo.MarkerInfo> markers = JandiMarkerDatabaseManager.getInstance(context).getMarkers(teamId, roomId);

        int unreadCount = Observable.from(markers)
                .filter(markerInfo -> markerInfo.getLastLinkId() < linkId)
                .count()
                .toBlocking()
                .first();

        return unreadCount;
    }
}
