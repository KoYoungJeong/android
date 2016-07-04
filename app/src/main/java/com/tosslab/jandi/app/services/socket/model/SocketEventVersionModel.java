package com.tosslab.jandi.app.services.socket.model;

import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

public class SocketEventVersionModel {

    public static <T extends EventHistoryInfo> boolean validVersion(T object) {

        Class<?> klass = object.getClass();
        Version annotation = klass.getAnnotation(Version.class);
        if (annotation == null) {
            return false;
        } else {
            int versionValue = object.getVersion();
            return annotation.value() == versionValue;
        }
    }
}
