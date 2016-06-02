package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class UnreadCountUtil {

    public static Observable<Long> getUnreadCount(long teamId, long roomId, long linkId,
                                                  long fromEntityId, long myId) {


        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                long roomMarkerCount = MarkerRepository.getRepository().getRoomMarkerCount(roomId, linkId);
                subscriber.onNext(roomMarkerCount);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .map(roomMarkerCount -> {
                    ResRoomInfo.MarkerInfo myMarker = MarkerRepository.getRepository().getMyMarker(roomId, myId);
                    long myLastLinkId = myMarker.getLastLinkId();
                    if ((fromEntityId == myId
                            && (0 <= myLastLinkId && myLastLinkId < linkId))) {
                        return roomMarkerCount - 1;
                    } else {
                        return roomMarkerCount;
                    }
                });

    }
}
