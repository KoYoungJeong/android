package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResConfig {
    public Versions versions;
    public Maintenance maintenance;
    public Versions latestVersions;

    @Override
    public String toString() {
        return "ResConfig{" +
                "versions=" + versions +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Versions {
        public String ios;
        public int android;
        public String osx;
        public String web;
        public String windows;

        @Override
        public String toString() {
            return "Versions{" +
                    "ios='" + ios + '\'' +
                    ", android=" + android +
                    ", osx='" + osx + '\'' +
                    ", web='" + web + '\'' +
                    ", windows='" + windows + '\'' +
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
