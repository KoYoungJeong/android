package com.tosslab.jandi.app;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class JandiConstants {
    public static final String AUTH_HEADER      = "Authorization";

    public static final String PUSH_REFRESH_ACTION = "com.tosslab.jandi.app.Push";

    // Push로 부터 넘어온 MainActivity의 Extra
    public static final String EXTRA_ENTITY_TYPE    = "entityType";
    public static final String EXTRA_ENTITY_ID      = "entityId";
    public static final String EXTRA_IS_FROM_PUSH   = "isFromPush";

    // SharedPreference Key 값
    public static final String PREF_NAME        = "JandiPref";
    public static final String PREF_TOKEN       = "token";
    public static final String PREF_LOGIN_ID    = "loginId";
    public static final String PREF_PUSH_ENTITY = "pushEntity";
    public static final String PREF_HAS_READ_TUTORIAL   = "hasReadTutorial";
    public static final String PREF_BADGE_COUNT  = "badgeCount";

    // GCM
    public static final String PREF_NAME_GCM        = "JandiGcm";
    public static final String SENDER_ID            = "558811220581";
    public static final String PREF_PUSH_TOKEN      = "registrationId";
    public static final String PREF_PUSH_TOKEN_TBU  = "registrationIdToBeUpdated";
    public static final String PREF_APP_VERSION     = "priorAppVersion";

    public static final int TYPE_PUBLIC_TOPIC   = 0x00;
    public static final int TYPE_DIRECT_MESSAGE = 0x01;
    public static final int TYPE_PRIVATE_TOPIC  = 0x02;

    public static final int TYPE_UPLOAD_GALLERY         = 0x00;
    public static final int TYPE_UPLOAD_TAKE_PHOTO      = 0x01;
    public static final int TYPE_UPLOAD_EXPLORER        = 0x02;
    public static final int TYPE_FILE_DETAIL_REFRESH    = 0x03;

    public static final int NOTIFICATION_ID = 100;
}
