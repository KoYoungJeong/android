package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.sql.SQLException;

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
                Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = getHelper().getDao(ResRoomInfo.MarkerInfo.class);
                ResRoomInfo.MarkerInfo markerInfo = markerInfoDao.queryBuilder()
                        .where()
                        .eq("memberId", memberId)
                        .queryForFirst();

                if (markerInfo != null) {

                    UpdateBuilder<ResRoomInfo.MarkerInfo, ?> markerInfoUpdateBuilder = markerInfoDao.updateBuilder();
                    markerInfoUpdateBuilder.updateColumnValue("lastLinkId", lastLinkId)
                            .where()
                            .eq("roomId", roomId)
                            .and()
                            .eq("memberId", memberId);
                    return markerInfoUpdateBuilder.update() > 0;
                } else {
                    ResRoomInfo roomInfo = roomInfoDao.queryBuilder()
                            .where()
                            .eq("roomId", roomId)
                            .and()
                            .eq("teamId", teamId)
                            .queryForFirst();
                    ResRoomInfo.MarkerInfo data = new ResRoomInfo.MarkerInfo();
                    data.setRoom(roomInfo);
                    data.setLastLinkId(lastLinkId);
                    data.setMemberId(memberId);
                    return markerInfoDao.create(data) > 0;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

        });
    }

    public long getRoomMarkerCount(long roomId, long linkId) {
        return execute(() -> {
            try {
                Dao<ResRoomInfo.MarkerInfo, ?> dao = getHelper().getDao(ResRoomInfo.MarkerInfo.class);
                return dao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .and()
                        .ge("lastLinkId", 0)
                        .and()
                        .lt("lastLinkId", linkId)
                        .countOf();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0L;

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
