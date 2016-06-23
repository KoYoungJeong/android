package com.tosslab.jandi.app.services.socket.model;

import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.lang.reflect.Field;

public class SocketEventVersionModel {

    public static boolean validVersion(Object object) {

        Class<?> klass = object.getClass();
        Version annotation = klass.getAnnotation(Version.class);
        if (annotation == null) {
            return false;
        } else {
            try {
                int versionValue;

                if (object instanceof EventHistoryInfo) {
                    versionValue = ((EventHistoryInfo) object).getVersion();
                } else {

                    Field version = null;

                    while (version == null && klass != null && klass != Object.class) {

                        try {
                            version = klass.getDeclaredField("version");
                        } catch (NoSuchFieldException e) {
                            klass = klass.getSuperclass();
                        }

                    }


                    if (version == null) {
                        return false;
                    }
                    version.setAccessible(true);

                    versionValue = version.getInt(object);
                    version.setAccessible(false);
                }

                if (annotation.value() == versionValue) {
                    return true;
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
