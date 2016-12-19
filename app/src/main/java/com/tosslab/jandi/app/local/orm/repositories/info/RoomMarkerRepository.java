package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import io.realm.RealmList;
import io.realm.RealmResults;

public class RoomMarkerRepository extends RealmRepository {

    private static RoomMarkerRepository instance;

    synchronized public static RoomMarkerRepository getInstance() {
        if (instance == null) {
            instance = new RoomMarkerRepository();
        }
        return instance;
    }

    public boolean upsertRoomMarker(long roomId, long memberId, long lastLinkId) {
        return execute(realm -> {

            Marker marker = realm.where(Marker.class).equalTo("id", roomId + "_" + memberId).findFirst();
            if (marker != null) {
                realm.executeTransaction(realm1 -> {
                    if (marker.getReadLinkId() < lastLinkId) {
                        marker.setReadLinkId(lastLinkId);
                    }
                });
            } else {

                realm.executeTransaction(realm1 -> {
                    Marker marker2 = realm.createObject(Marker.class, roomId + "_" + memberId);
                    marker2.setReadLinkId(lastLinkId);
                    marker2.setMemberId(memberId);
                    marker2.setRoomId(roomId);

                    Chat chat = realm.where(Chat.class)
                            .equalTo("id", roomId)
                            .equalTo("memberIds.value", memberId)
                            .findFirst();

                    Topic topic = realm.where(Topic.class)
                            .equalTo("id", roomId)
                            .equalTo("memberIds.value", memberId)
                            .findFirst();

                    if (chat != null) {
                        if (chat.getMarkers() != null) {
                            chat.getMarkers().add(marker2);
                        } else {
                            chat.setMarkers(new RealmList<>(marker2));
                        }
                    } else if (topic != null) {
                        if (topic.getMarkers() != null) {
                            topic.getMarkers().add(marker2);
                        } else {
                            topic.setMarkers(new RealmList<>(marker2));
                        }
                    }
                });
            }

            return true;

        });
    }

    public Marker getMarker(long roomId, long memberId) {
        return execute(realm -> {
            Marker it = realm.where(Marker.class)
                    .equalTo("id", roomId + "_" + memberId)
                    .findFirst();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return null;
            }
        });

    }

    public long getRoomMarkerCount(long roomId, long linkId) {
        return execute(realm -> realm.where(Marker.class)
                .equalTo("roomId", roomId)
                .greaterThanOrEqualTo("readLinkId", 0)
                .lessThan("readLinkId", linkId)
                .count());

    }

    public boolean deleteMarker(long roomId, long memberId) {
        return execute(realm -> {

            Marker marker = realm.where(Marker.class).equalTo("id", roomId + "_" + memberId).findFirst();
            if (marker != null) {
                realm.executeTransaction(realm1 -> marker.deleteFromRealm());
                return true;
            }

            return false;
        });
    }

    public boolean deleteMarkers(long roomId) {
        return execute(realm -> {
            RealmResults<Marker> roomId1 = realm.where(Marker.class)
                    .equalTo("roomId", roomId)
                    .findAll();
            if (!roomId1.isEmpty()) {
                realm.executeTransaction(realm1 -> roomId1.deleteAllFromRealm());
                return true;
            }
            return false;
        });

    }
}
