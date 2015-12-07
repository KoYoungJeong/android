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
    private static final String SERVICE_DOMAIN = "i-bot.jandi.io";
    private static final String SERVICE_FILE_DOMAIN = "files.jandi.io";
    private static final String SERVICE_FILE_UPLOAD_DOMAIN = "upload.jandi.io";
    private static final String SERVICE_PORT = "443";
    public static final String SERVICE_ROOT_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_DOMAIN + ":" + SERVICE_PORT + "/";
    public static final String SERVICE_INNER_API_URL = SERVICE_ROOT_URL + "inner-api";
    public static final String SERVICE_FILE_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_FILE_DOMAIN + ":" + SERVICE_PORT + "/";
    public static final String SERVICE_FILE_UPLOAD_URL =
            SERVICE_PROTOCOL + "://" + SERVICE_FILE_UPLOAD_DOMAIN + ":" + SERVICE_PORT + "/";
    private static final String SOCKET_PROTOCOL = "https";
    private static final String SOCKET_DOMAIN = "ws.jandi.io";
    private static final String SOCKET_PORT = "443";
    public static final String SOCKET_ROOT_URL =
            SOCKET_PROTOCOL + "://" + SOCKET_DOMAIN + ":" + SOCKET_PORT + "/";

}