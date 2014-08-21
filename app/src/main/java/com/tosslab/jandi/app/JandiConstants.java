package com.tosslab.jandi.app;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class JandiConstants {
//    public static final String SERVICE_ROOT_URL = "https://192.168.0.19:3000/";
    public static final String SERVICE_ROOT_URL = "https://dev.jandi.com:2323/";
//    public static final String SERVICE_ROOT_URL = "https://jandi.com/";

    // Push로 부터 넘어온 MainActivity의 Extra
    public static final String EXTRA_CDP_TYPE   = "cdpTypeFromPush";
    public static final String EXTRA_CDP_ID     = "cdpIdFromPush";

    // SharedPreference Key 값
    public static final String PREF_NAME        = "JandiPref";
    public static final String PREF_TOKEN       = "token";
    public static final String PREF_LOGIN_ID    = "loginId";

    public static final String PREF_NAME_GCM    = "JandiGcm";
    public static final String SENDER_ID        = "558811220581";
    public static final String PREF_REG_ID      = "registrationId";
    public static final String PREF_APP_VERSION = "JandiVersion";

    public static final int TYPE_CHANNEL        = 0x00;
    public static final int TYPE_DIRECT_MESSAGE = 0x01;
    public static final int TYPE_PRIVATE_GROUP  = 0x02;

    public static final int TYPE_UPLOAD_GALLERY     = 0x00;
    public static final int TYPE_UPLOAD_EXPLORER    = 0x01;
    public static final int TYPE_FILE_DETAIL_REFRESH    = 0x02;

    // ERROR
    public static final int BAD_REQUEST     = 400;
}
