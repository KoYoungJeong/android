package com.tosslab.jandi.app;

import com.tosslab.jandi.app.network.DomainUtil;

public class JandiConstantsForFlavors {
    // These constants are for REAL sevice
    public static final String SERVICE_DOMAIN_BASE = "jandi.com";

    public static final String GA_TRACK_ID = "UA-53634725-3";

    public static final String INTERCOM_API_KEY = "android_sdk-40917d53dc07205e1515f909a14a94752d410f70";
    public static final String INTERCOM_API_ID = "yt1d5jat";


    private static String getServiceDomain() {
        return "i2." + DomainUtil.getDomain();
    }

    private static String getServiceFileDomain() {
        return "files." + DomainUtil.getDomain();
    }

    private static String getServiceFileUploadDomain() {
        return "upload." + DomainUtil.getDomain();
    }

    private static String getSocketDomain() {
        return "ws." + DomainUtil.getDomain();
    }

    private static String getServiceRootUrl() {
        return "https://" + getServiceDomain() + ":443/";
    }

    public static String getServiceFileUrl() {
        return "https://" + getServiceFileDomain() + ":443/";
    }

    public static String getServiceFileUploadUrl() {
        return "https://" + getServiceFileUploadDomain() + ":443/";
    }

    public static String getServiceBaseUrl() {
        return "https://www." + DomainUtil.getDomain() + ":443/";
    }

    public static String getServiceInnerApiUrl() {
        return getServiceRootUrl() + "inner-api/";
    }

    public static String getSocketRootUrl() {
        return "https://" + getSocketDomain() + ":443/";
    }

    public static class Push {
        public static final String GCM_SENDER_ID = "252549028791";
        public static final String BAIDU_API_KEY = "dkDbCUo6nyYsXGD8cPxjcXVe";
    }
}
