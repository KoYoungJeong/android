package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class UnreadCountUtil {

    public static Observable<Long> getUnreadCount(long roomId, long linkId,
                                                  long fromEntityId, long myId) {


        return Observable.fromCallable(() -> {
            return RoomMarkerRepository.getInstance().getRoomMarkerCount(roomId, linkId);
        }).subscribeOn(Schedulers.io())
                .map(roomMarkerCount -> {
                    long myLastLinkId = RoomMarkerRepository.getInstance().getMarkerReadLinkId(roomId, myId);
                    if (myLastLinkId <= -1) {
                        return roomMarkerCount - 1;
                    }
                    if ((fromEntityId == myId
                            && (0 <= myLastLinkId && myLastLinkId < linkId))) {
                        return roomMarkerCount - 1;
                    } else {
                        return roomMarkerCount;
                    }
                }).onErrorReturn(throwable -> 0L);

    }
}
