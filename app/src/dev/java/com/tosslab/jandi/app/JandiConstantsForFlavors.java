package com.tosslab.jandi.app;

import com.tosslab.jandi.app.network.DomainUtil;

public class JandiConstantsForFlavors {
    public static final String SERVICE_DOMAIN_BASE = "jandi.io";
    public static final String GA_TRACK_ID = "UA-29745379-2";
    public static final String INTERCOM_API_KEY = "android_sdk-1dc44febffdbbc7fab963de6f5e093f672cba0a1";
    public static final String INTERCOM_API_ID = "xjjmliv4";
    // These constants are just for DEBUG mode
    private static final String SERVICE_PROTOCOL = "https";
    private static final String SERVICE_PORT = "443";

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
        return SERVICE_PROTOCOL + "://" + getServiceDomain() + ":" + SERVICE_PORT + "/";
    }

    public static String getServiceFileUrl() {
        return SERVICE_PROTOCOL + "://" + getServiceFileDomain() + ":" + SERVICE_PORT + "/";
    }

    public static String getServiceFileUploadUrl() {
        return SERVICE_PROTOCOL + "://" + getServiceFileUploadDomain() + ":" + SERVICE_PORT + "/";
    }

    public static String getServiceBaseUrl() {
        return SERVICE_PROTOCOL + "://www." + DomainUtil.getDomain() + ":" + SERVICE_PORT + "/";
    }

    public static String getServiceInnerApiUrl() {
        return getServiceRootUrl() + "inner-api/";
    }

    public static String getSocketRootUrl() {
        return SERVICE_PROTOCOL + "://" + getSocketDomain() + ":" + SERVICE_PORT + "/";
    }


    public static class Push {

        public static final String GCM_SENDER_ID = "648623146882";
        public static final String BAIDU_API_KEY = "xMfI1LkL64AmDCGsw9kgbenb";
    }
}