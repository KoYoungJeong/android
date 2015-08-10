package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.util.Collection;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class UnreadCountUtil {

    public static int getUnreadCount(int teamId, int roomId, int linkId,
                                     int fromEntityId, int myId) {

        Collection<ResRoomInfo.MarkerInfo> markers =
                MarkerRepository.getRepository().getRoomMarker(teamId, roomId);

        int unreadCount =
                Observable.from(markers)
                        .filter(markerInfo ->
                                // -1 이면 읽음 처리
                                markerInfo.getLastLinkId() >= 0)

                        .filter(markerInfo ->
                                // 유저의 마지막 마커가 크면 읽음 처리
                                markerInfo.getLastLinkId() < linkId)

                        .filter(markerInfo ->
                                // 내 메세지이면 읽음 처리
                                !(fromEntityId == myId && markerInfo.getMemberId() == myId))
                        .count()
                        .toBlocking()
                        .first();

        return unreadCount;
    }
}
