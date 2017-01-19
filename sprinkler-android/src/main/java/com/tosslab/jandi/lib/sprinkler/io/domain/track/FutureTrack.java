package com.tosslab.jandi.lib.sprinkler.io.domain.track;

import com.tosslab.jandi.lib.sprinkler.constant.IdentifierKey;
import com.tosslab.jandi.lib.sprinkler.io.domain.event.Event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tonyjs on 15. 7. 22..
 * <p>
 * Database 에 삽입 될 Track
 */
public class FutureTrack implements Serializable {
    private static final long serialVersionUID = -19881222;

    private String event;
    private Map<String, Object> identifiersMap = new HashMap<>();
    private String platform;
    private Map<String, Object> propertiesMap = new HashMap<>();
    private long time;
    private String version;

    FutureTrack() {
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Map<String, Object> getIdentifiersMap() {
        return identifiersMap;
    }

    public void setIdentifiersMap(Map<String, Object> identifiersMap) {
        this.identifiersMap = identifiersMap;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Map<String, Object> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(Map<String, Object> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public java.lang.String toString() {
        return "FutureTrack{" +
                "event='" + event + '\'' +
                ", identifiersMap=" + identifiersMap +
                ", platform='" + platform + '\'' +
                ", propertiesMap=" + propertiesMap +
                ", time=" + time +
                ", version='" + version + '\'' +
                '}';
    }

    public static final class Builder {
        private Event event;
        private Map<String, Object> identifiersMap = new HashMap<>();
        private Map<String, Object> propertiesMap = new HashMap<>();
        private String[] availablePropertyKeys;

        public Builder event(Event event) {
            this.event = event;
            availablePropertyKeys = event.getAvailablePropertyKeys();
            return this;
        }

        public Builder deviceId(String deviceId) {
            identifiersMap.put(IdentifierKey.DEVICE_ID, deviceId);
            return this;
        }


        public Builder accountId(String accountId) {
            identifiersMap.put(IdentifierKey.ACCOUNT_ID, accountId);
            return this;
        }

        public Builder memberId(long memberId) {
            identifiersMap.put(IdentifierKey.MEMBER_ID, memberId);
            return this;
        }

        public Builder property(String key, Object value) {
            if (!containsKey(key)) {
                return this;
            }

            propertiesMap.put(key, value);
            return this;
        }

        private boolean containsKey(String key) {
            if (availablePropertyKeys != null && availablePropertyKeys.length > 0) {
                for (String pk : availablePropertyKeys) {
                    if (pk.equals(key)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public FutureTrack build() {
            if (event == null) {
                throw new IllegalArgumentException(
                        "FutureTrack.Builder - Can not build without \'event\'. \'event\' must be set up.");
            }
            FutureTrack track = new FutureTrack();
            track.setEvent(event.getName());
            track.setIdentifiersMap(identifiersMap);
            track.setPropertiesMap(propertiesMap);

            return track;
        }

    }
}
