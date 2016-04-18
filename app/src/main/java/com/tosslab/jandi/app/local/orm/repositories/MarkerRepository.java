package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
public class MarkerRepository extends LockExecutorTemplate {

    private static MarkerRepository repository;

    synchronized public static MarkerRepository getRepository() {

        if (repository == null) {
            repository = new MarkerRepository();
        }
        return repository;
    }

    public boolean upsertRoomInfo(ResRoomInfo roomInfo) {
        return execute(() -> {
            try {

                Dao<ResRoomInfo, ?> roomInfoDao = getHelper().getDao(ResRoomInfo.class);
                roomInfoDao.createOrUpdate(roomInfo);

                Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = getHelper().getDao(ResRoomInfo.MarkerInfo.class);
                DeleteBuilder<ResRoomInfo.MarkerInfo, ?> deleteBuilder = markerInfoDao.deleteBuilder();
                deleteBuilder.where().eq("roomId", roomInfo.getId());
                deleteBuilder.delete();

                markerInfoDao.callBatchTasks(() -> {
                    for (ResRoomInfo.MarkerInfo markerInfo : roomInfo.getMarkers()) {
                        markerInfo.setRoom(roomInfo);
                        markerInfoDao.create(markerInfo);
                    }
                    return null;
                });

                return true;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        });
    }

    public boolean upsertRoomMarker(long teamId, long roomId, long memberId, long lastLinkId) {

        return execute(() -> {
            try {

                Dao<ResRoomInfo, ?> roomInfoDao = getHelper().getDao(ResRoomInfo.class);
                ResRoomInfo roomInfo = roomInfoDao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("teamId", teamId)
                        .queryForFirst();

                if (roomInfo != null) {
                    boolean find = false;
                    ResRoomInfo.MarkerInfo savedMarker = null;
                    for (ResRoomInfo.MarkerInfo markerInfo : roomInfo.getMarkers()) {
                        if (markerInfo.getMemberId() == memberId) {
                            find = true;
                            markerInfo.setLastLinkId(lastLinkId);
                            savedMarker = markerInfo;
                            break;
                        }

                    }

                    Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = getHelper().getDao(ResRoomInfo.MarkerInfo.class);

                    if (!find) {
                        ResRoomInfo.MarkerInfo markerInfo = new ResRoomInfo.MarkerInfo();
                        markerInfo.setLastLinkId(lastLinkId);
                        markerInfo.setMemberId(memberId);
                        markerInfo.setRoom(roomInfo);
                        return markerInfoDao.create(markerInfo) > 0;
                    } else {
                        return markerInfoDao.update(savedMarker) > 0;
                    }

                } else {
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

        });
    }

    public Collection<ResRoomInfo.MarkerInfo> getRoomMarker(long teamId, long roomId) {
        return execute(() -> {
            try {
                Dao<ResRoomInfo, ?> roomInfoDao = getHelper().getDao(ResRoomInfo.class);
                ResRoomInfo roomInfo = roomInfoDao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .queryForFirst();

                if (roomInfo != null) {
                    return roomInfo.getMarkers();
                } else {
                    return new ArrayList<ResRoomInfo.MarkerInfo>();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<ResRoomInfo.MarkerInfo>();

        });
    }

    public int deleteRoomMarker(int roomId, int memberId) {
        return execute(() -> {
            try {
                Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = getHelper().getDao(ResRoomInfo.MarkerInfo.class);

                DeleteBuilder<ResRoomInfo.MarkerInfo, ?> deleteBuilder = markerInfoDao.deleteBuilder();
                deleteBuilder.where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("memberId", memberId);
                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public ResRoomInfo.MarkerInfo getMyMarker(long roomId, long myId) {
        return execute(() -> {
            ResRoomInfo.MarkerInfo markerInfo = null;
            try {
                Dao<ResRoomInfo.MarkerInfo, ?> dao = getHelper().getDao(ResRoomInfo.MarkerInfo.class);
                markerInfo = dao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("memberId", myId)
                        .queryForFirst();


            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (markerInfo == null) {
                markerInfo = new ResRoomInfo.MarkerInfo();
                markerInfo.setLastLinkId(-1);
            }

            return markerInfo;

        });
    }
}
