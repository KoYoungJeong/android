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
                markerInfoDao.createOrUpdate(markerInfo);
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

            Dao<ResRoomInfo, ?> roomInfoDao = helper.getDao(ResRoomInfo.class);
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

                Dao<ResRoomInfo.MarkerInfo, ?> markerInfoDao = helper.getDao(ResRoomInfo.MarkerInfo.class);

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
        } finally {
            lock.unlock();
        }
    }

    public Collection<ResRoomInfo.MarkerInfo> getRoomMarker(int teamId, int roomId) {
        lock.lock();
        try {
            Dao<ResRoomInfo, ?> roomInfoDao = helper.getDao(ResRoomInfo.class);
            ResRoomInfo roomInfo = roomInfoDao.queryBuilder()
                    .where()
                    .eq("teamId", teamId)
                    .and()
                    .eq("roomId", roomId)
                    .queryForFirst();

            if (roomInfo != null) {
                return roomInfo.getMarkers();
            } else {
                return new ArrayList<>();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
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

    public ResRoomInfo.MarkerInfo getMyMarker(int roomId, int myId) {
        lock.lock();
        try {
            Dao<ResRoomInfo.MarkerInfo, ?> dao = helper.getDao(ResRoomInfo.MarkerInfo.class);
            return dao.queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .and()
                    .eq("memberId", myId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        ResRoomInfo.MarkerInfo markerInfo = new ResRoomInfo.MarkerInfo();
        markerInfo.setLastLinkId(-1);
        return markerInfo;

    }
}
