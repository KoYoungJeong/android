package com.tosslab.jandi.app.services.socket.model;

import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketEventVersionModel {

    private static Map<Class, Version> versionMapper;

    static {
        versionMapper = new ConcurrentHashMap<>();
    }

    public static <T extends EventHistoryInfo> boolean validVersion(T object) {

        Class<?> klass = object.getClass();
        Version annotation;
        if (versionMapper.containsKey(klass)) {
            annotation = versionMapper.get(klass);
        } else {
            annotation = klass.getAnnotation(Version.class);
            if (annotation != null) {
                versionMapper.put(klass, annotation);
            }
        }
        if (annotation == null) {
            return false;
        } else {
            int versionValue = object.getVersion();
            return annotation.value() == versionValue;
        }
    }
}
