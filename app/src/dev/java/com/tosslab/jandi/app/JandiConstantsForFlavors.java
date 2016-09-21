package com.tosslab.jandi.app;

/**
 * Created by justinygchoi on 14. 10. 31..
 */
public class JandiConstantsForFlavors {
    public static final String GA_TRACK_ID = "UA-29745379-2";
    public static final String MIXPANEL_MEMBER_TRACK_ID = "081e1e9730e547f43bdbf59be36a4e31";
    public static final String MIXPANEL_ACCOUNT_TRACK_ID = "322e7bff6504a2f680c0f2d4a3545319";
    public static final String NEWRELIC_TOKEN_ID = "";
    public static final String PARSE_APPLICATION_ID = "F61CAJEi4bXQ4S0me694noykr7ALCnFjEaR7yQcf";
    public static final String PARSE_CLIENT_KEY = "RP8MlBZfYVHRQiaZli0IEDzkpNSWulesgmSqmxke";
    // These constants are just for DEBUG mode
    private static final String SERVICE_PROTOCOL = "https";
    private static final String SERVICE_DOMAIN_SUB = "i2";
    private static final String SERVICE_DOMAIN_BASE = "jandi.io";
    private static final String SERVICE_PORT = "443";
    private static final String SERVICE_DOMAIN = SERVICE_DOMAIN_SUB + "." + SERVICE_DOMAIN_BASE;
    private static final String SERVICE_FILE_DOMAIN = "files" + "." + SERVICE_DOMAIN_BASE;
    private static final String SERVICE_BASE_DOMAIN = "www" + "." + SERVICE_DOMAIN_BASE;
    private static final String SERVICE_FILE_UPLOAD_DOMAIN = "upload" + "." + SERVICE_DOMAIN_BASE;
    public static final String SERVICE_ROOT_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN + ":" + SERVICE_PORT + "/";
    public static final String SERVICE_INNER_API_URL = SERVICE_ROOT_URL + "inner-api/";
    public static final String SERVICE_FILE_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_FILE_DOMAIN + ":" + SERVICE_PORT + "/";
    public static final String SERVICE_FILE_UPLOAD_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_FILE_UPLOAD_DOMAIN + ":" + SERVICE_PORT + "/";
    public static final String SERVICE_BASE_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_BASE_DOMAIN + ":" + SERVICE_PORT + "/";

    private static final String SOCKET_DOMAIN = "ws" + "." + SERVICE_DOMAIN_BASE;
    public static final String SOCKET_ROOT_URL =
            SERVICE_PROTOCOL + "://" + SOCKET_DOMAIN + ":" + SERVICE_PORT + "/";

    public static final String INTERCOM_API_KEY = "android_sdk-1dc44febffdbbc7fab963de6f5e093f672cba0a1";
    public static final String INTERCOM_API_ID = "xjjmliv4";

    public static class Push {

        public static final String GCM_SENDER_ID = "648623146882";
        public static final String BAIDU_API_KEY = "xMfI1LkL64AmDCGsw9kgbenb";
    }
}