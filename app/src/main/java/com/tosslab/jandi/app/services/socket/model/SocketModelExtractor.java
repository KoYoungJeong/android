package com.tosslab.jandi.app.services.socket.model;


import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;

public class SocketModelExtractor {

    public static <T extends EventHistoryInfo> T getObject(Object object, Class<T> clazz) throws Exception {
        return getObject(object, clazz, true, true);
    }

    public static <T extends EventHistoryInfo> T getObjectWithoutCheckTeam(Object object, Class<T> clazz) throws Exception {
        return getObject(object, clazz, true, false);
    }

    public static <T extends EventHistoryInfo> T getObject(Object object, Class<T> clazz, boolean checkVersion, boolean checkTeamId) throws Exception {
        T t;
        if (object.getClass() != clazz) {
            t = JacksonMapper.getInstance().getObjectMapper().readValue(object.toString(), clazz);
        } else {
            t = (T) object;
        }
        if (checkVersion) {
            throwExceptionIfInvaildVersion(t);
        }

        if (checkTeamId) {
            throwExceptionIfInvaildTeamId(t);
        }
        return t;
    }

    private static <T> void throwExceptionIfInvaildTeamId(T t) throws Exception {
        if (t instanceof EventHistoryInfo) {
            long teamId = ((EventHistoryInfo) t).getTeamId();
            if (teamId != 0
                    && teamId != AccountRepository.getRepository().getSelectedTeamId()) {
                throw new Exception("Ignore Team : " + t.getClass().getName());
            }
        }
    }

    public static <T extends EventHistoryInfo> void throwExceptionIfInvaildVersion(T object) throws Exception {
        if (!SocketEventVersionModel.validVersion(object)) {
            throw new Exception("Invalid Version : " + object.getClass().getName());
        }
    }
}
