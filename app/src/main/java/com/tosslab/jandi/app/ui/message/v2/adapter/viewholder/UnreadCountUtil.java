package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.util.Collection;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class UnreadCountUtil {

    public static Observable<Integer> getUnreadCount(long teamId, long roomId, long linkId,
                                                     long fromEntityId, long myId) {


        return Observable.create(new Observable.OnSubscribe<ResRoomInfo.MarkerInfo>() {
            @Override
            public void call(Subscriber<? super ResRoomInfo.MarkerInfo> subscriber) {
                Collection<ResRoomInfo.MarkerInfo> roomMarker = MarkerRepository.getRepository().getRoomMarker(roomId);
                for (ResRoomInfo.MarkerInfo markerInfo : roomMarker) {
                    subscriber.onNext(markerInfo);
                }

                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .filter(markerInfo ->
                        // -1 이면 읽음 처리
                        markerInfo.getLastLinkId() >= 0)

                .filter(markerInfo ->
                        // 유저의 마지막 마커가 크면 읽음 처리
                        markerInfo.getLastLinkId() < linkId)

                .filter(markerInfo ->
                        // 내 메세지이면 읽음 처리
                        !(fromEntityId == myId && markerInfo.getMemberId() == myId))
                .count();

    }
}
