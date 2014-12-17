package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
public class ResConfig {
    public Version versions;

    @Override
    public String toString() {
        return "ResConfig{" +
                "versions=" + versions +
                '}';
    }

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
}
