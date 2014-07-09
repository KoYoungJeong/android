package com.tosslab.jandi.app;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class JandiConstants {
    public static final String SERVICE_ROOT_URL = "https://112.219.215.146:3000/";
//    public static final String SERVICE_ROOT_URL = "https://192.168.0.19:3000/";
//    public static final String SERVICE_ROOT_URL = "https://192.168.0.10:3000/";

    // SharedPreference Key 값
    public static final String PREF_NAME        = "JandiPref";
    public static final String PREF_TOKEN       = "token";

    public static final String PREF_NAME_GCM    = "JandiGcm";
    public static final String SENDER_ID        = "558811220581";
    public static final String PREF_REG_ID      = "registrationId";
    public static final String PREF_APP_VERSION = "0.1";

    public static final int TYPE_CHANNEL        = 0x00;
    public static final int TYPE_DIRECT_MESSAGE = 0x01;
    public static final int TYPE_PRIVATE_GROUP  = 0x02;
    public static final int TYPE_TITLE_JOINED_CHANNEL   = 0x10;
    public static final int TYPE_TITLE_UNJOINED_CHANNEL = 0x11;
    public static final int TYPE_TITLE_DIRECT_MESSAGE   = 0x12;
    public static final int TYPE_TITLE_PRIVATE_GROUP    = 0x13;

    public static final int TYPE_UPLOAD_GALLERY     = 0x00;
    public static final int TYPE_UPLOAD_EXPLORER    = 0x03;

    public static final String TYPE_CHANNEL_STRING          = "channel";
    public static final String TYPE_DIRECT_MESSAGE_STRING   = "user";
    public static final String TYPE_PRIVATE_GROUP_STRING    = "parivateGroup";

    // Search 관련
    public static final int TYPE_SEARCH_EVERYONE    = 0x00;
    public static final int TYPE_SEARCH_SPECIFIC    = 0x01;
    public static final int TYPE_SEARCH_IMAGES      = 0x10;
}