package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import java.util.ArrayList;
import java.util.List;

public class RoomMarkerRepository extends LockTemplate {

    private static LongSparseArray<RoomMarkerRepository> instance;
    private final long teamId;

    private RoomMarkerRepository(long teamId) {
        super();
        this.teamId = teamId;
    }

    synchronized public static RoomMarkerRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            RoomMarkerRepository value = new RoomMarkerRepository(teamId);
            instance.put(teamId, value);
            return value;

        }
    }

    synchronized public static RoomMarkerRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    private List<Marker> getRoomMarkers(long roomId) {
        return execute(() -> {
            if (ChatRepository.getInstance(teamId).hasChat(roomId)) {
                Chat chat = ChatRepository.getInstance(teamId).getChat(roomId);
                if (chat.getMarkers() == null) {
                    chat.setMarkers(new ArrayList<>());
                }

                return chat.getMarkers();

            } else if (TopicRepository.getInstance(teamId).isTopic(roomId)) {
                Topic topic = TopicRepository.getInstance(teamId).getTopic(roomId);
                if (topic.getMarkers() == null) {
                    topic.setMarkers(new ArrayList<>());
                }

                return topic.getMarkers();

            } else {
                return null;
            }
        });
    }

    public boolean upsertRoomMarker(long roomId, long memberId, long lastLinkId) {
        return execute(() -> {

            List<Marker> markers = getRoomMarkers(roomId);

            if (markers == null) {
                return false;
            }

            boolean hasMarker = false;
            for (Marker marker : markers) {
                if (marker.getMemberId() == memberId) {
                    marker.setReadLinkId(lastLinkId);
                    hasMarker = true;
                }
            }

            if (!hasMarker) {
                Marker marker = new Marker();
                marker.setMemberId(memberId);
                marker.setReadLinkId(lastLinkId);
                markers.add(marker);
            }


            return true;

        });
    }

    public Marker getMarker(long roomId, long memberId) {
        return execute(() -> {

            List<Marker> markers = getRoomMarkers(roomId);

            if (markers == null) {
                return null;
            }

            for (Marker marker : markers) {
                if (marker.getMemberId() == memberId) {
                    return marker;
                }
            }
            return null;
        });

    }

    public long getMarkerReadLinkId(long roomId, long memberId) {
        return execute(() -> {
            Marker marker = getMarker(roomId, memberId);
            if (marker != null) {
                return marker.getReadLinkId();
            } else {
                return -1L;
            }
        });

    }

    public long getRoomMarkerCount(long roomId, long linkId) {
        return execute(() -> {
            List<Marker> markers = getRoomMarkers(roomId);

            long count = 0;
            for (Marker marker : markers) {
                long readLinkId = marker.getReadLinkId();
                if (readLinkId >= 0 && readLinkId < linkId) {
                    ++count;
                }
            }
            return count;

        });

    }

    public boolean deleteMarker(long roomId, long memberId) {
        return execute(() -> {

            List<Marker> roomMarkers = getRoomMarkers(roomId);
            if (roomMarkers != null) {
                for (int idx = roomMarkers.size() - 1; idx >= 0; idx--) {
                    if (roomMarkers.get(idx).getMemberId() == memberId) {
                        roomMarkers.remove(idx);
                        return true;
                    }
                }
            }

            return false;
        });
    }

    public boolean deleteMarkers(long roomId) {
        return execute(() -> {

            List<Marker> roomMarkers = getRoomMarkers(roomId);
            if (roomMarkers != null) {
                roomMarkers.clear();
            }
            return true;
        });

    }
}
