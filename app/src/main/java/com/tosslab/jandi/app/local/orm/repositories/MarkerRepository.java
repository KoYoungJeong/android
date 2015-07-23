package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
public class MarkerRepository {
    private static MarkerRepository repository;
    private final OrmDatabaseHelper helper;
    private final Lock lock;

    private MarkerRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();
    }

    public static MarkerRepository getRepository() {

        if (repository == null) {
            repository = new MarkerRepository();
        }
        return repository;
    }

    public boolean upsertRoomInfo(ResRoomInfo roomInfo) {
        lock.lock();
        try {

            Dao<ResRoomInfo, ?> roomInfoDao = helper.getDao(ResRoomInfo.class);
            roomInfoDao.createOrUpdate(roomInfo);

            Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = helper.getDao(ResRoomInfo.MarkerInfo.class);
            DeleteBuilder<ResRoomInfo.MarkerInfo, ?> deleteBuilder = markerInfoDao.deleteBuilder();
            deleteBuilder.where().eq("roomId", roomInfo.getId());
            deleteBuilder.delete();
            for (ResRoomInfo.MarkerInfo markerInfo : roomInfo.getMarkers()) {
                markerInfo.setRoom(roomInfo);
                markerInfoDao.create(markerInfo);
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean upsertRoomMarker(int teamId, int roomId, int memberId, int lastLinkId) {
        lock.lock();
        try {
            Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = helper.getDao(ResRoomInfo.MarkerInfo.class);
            ResRoomInfo.MarkerInfo markerInfo = markerInfoDao
                    .queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .and()
                    .eq("memberId", memberId)
                    .queryForFirst();
            if (markerInfo == null) {

                Dao<ResRoomInfo, ?> roomInfoDao = helper.getDao(ResRoomInfo.class);
                ResRoomInfo resRoomInfo = roomInfoDao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("teamId", teamId)
                        .queryForFirst();

                if (resRoomInfo == null) {
                    return false;
                }

                ResRoomInfo.MarkerInfo data = new ResRoomInfo.MarkerInfo();
                data.setLastLinkId(lastLinkId);
                data.setMemberId(memberId);
                data.setRoom(resRoomInfo);
                markerInfoDao.create(data);
                return true;
            } else {
                markerInfo.setLastLinkId(lastLinkId);
                markerInfoDao.update(markerInfo);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Collection<ResRoomInfo.MarkerInfo> getRoomMarker(int teamId, int roomId) {
        try {
            Dao<ResRoomInfo, ?> roomInfoDao = helper.getDao(ResRoomInfo.class);
            ResRoomInfo roomInfo = roomInfoDao.queryBuilder()
                    .where()
                    .eq("teamId", teamId)
                    .and()
                    .eq("roomId", roomId)
                    .queryForFirst();

            return roomInfo.getMarkers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int deleteRoomMarker(int roomId, int memberId) {
        lock.lock();
        try {
            Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = helper.getDao(ResRoomInfo.MarkerInfo.class);

            DeleteBuilder<ResRoomInfo.MarkerInfo, ?> deleteBuilder = markerInfoDao.deleteBuilder();
            deleteBuilder.where()
                    .eq("roomId", roomId)
                    .and()
                    .eq("memberId", memberId);
            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            lock.unlock();
        }

        return 0;
    }
}