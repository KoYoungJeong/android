package com.tosslab.jandi.app;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class JandiConstants {
    public static final String AUTH_HEADER      = "Authorization";
    public static final String PUSH_CHANNEL_PREFIX = "c";
    public static final String PUSH_REFRESH_ACTION = "com.tosslab.jandi.app.Push";

    // Push로 부터 넘어온 MainActivity의 Extra
    public static final String EXTRA_ENTITY_TYPE    = "entityType";
    public static final String EXTRA_ENTITY_ID      = "entityId";
    public static final String EXTRA_IS_FROM_PUSH   = "isFromPush";
    public static final String EXTRA_TEAM_ID        = "teamId";

    public static final String PARSE_MY_ENTITY_ID   = "myEntityId";
    public static final String PARSE_CHANNELS       = "channels";
    public static final String PARSE_ACTIVATION     = "activate";
    public static final String PARSE_ACTIVATION_ON  = "on";
    public static final String PARSE_ACTIVATION_OFF = "off";

    public static final int TYPE_PUBLIC_TOPIC   = 0x00;
    public static final int TYPE_DIRECT_MESSAGE = 0x01;
    public static final int TYPE_PRIVATE_TOPIC  = 0x02;

    public static final int TYPE_UPLOAD_GALLERY         = 0x00;
    public static final int TYPE_UPLOAD_TAKE_PHOTO      = 0x01;
    public static final int TYPE_UPLOAD_EXPLORER        = 0x02;
    public static final int TYPE_FILE_DETAIL_REFRESH    = 0x03;

    public static final int TYPE_INVITATION_EMAIL               = 0x01;
    public static final int TYPE_INVITATION_KAKAO               = 0x02;
    public static final int TYPE_INVITATION_LINE                = 0x03;
    public static final int TYPE_INVITATION_WECHAT              = 0x04;
    public static final int TYPE_INVITATION_FACEBOOK_MESSENGER  = 0x05;
    public static final int TYPE_INVITATION_COPY_LINK           = 0x06;

    public static final int NOTIFICATION_ID = 100;

    public static class RoomType {
        public static final String TYPE_USER = "users";
        public static final String TYPE_PUBLIC = "channels";
        public static final String TYPE_PRIVATE = "privateGroups";

    }
}
