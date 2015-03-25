package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResConfig {
    public Version versions;
    public Maintenance maintenance;

    @Override
    public String toString() {
        return "ResConfig{" +
                "versions=" + versions +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Version {
        public String ios;
        public int android;

        @Override
        public String toString() {
            return "Version{" +
                    "ios='" + ios + '\'' +
                    ", android=" + android +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Maintenance {
        public boolean status;
        public String msg;

        @Override
        public String toString() {
            return "Maintenance{" +
                    "status=" + status +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }
}
