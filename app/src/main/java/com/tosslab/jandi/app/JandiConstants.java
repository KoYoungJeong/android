package com.tosslab.jandi.app;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class JandiConstants {

    public static final String AUTH_HEADER = "Authorization";
    public static final String PUSH_CHANNEL_PREFIX = "c";
    public static final String PUSH_REFRESH_ACTION = "com.tosslab.jandi.app.Push";

    public static final String HTTP_ACCEPT_HEADER_V2 = "application/vnd.tosslab.jandi-v2+json";
    public static final String HTTP_ACCEPT_HEADER_V3 = "application/vnd.tosslab.jandi-v3+json";
    public static final String HTTP_ACCEPT_HEADER_V4 = "application/vnd.tosslab.jandi-v4+json";
    public static final String HTTP_ACCEPT_HEADER_DEFAULT = HTTP_ACCEPT_HEADER_V2;

    // Push로 부터 넘어온 MainActivity의 Extra
    public static final String EXTRA_ENTITY_TYPE = "entityType";
    public static final String EXTRA_ENTITY_ID = "entityId";
    public static final String EXTRA_IS_FROM_PUSH = "isFromPush";
    public static final String EXTRA_TEAM_ID = "teamId";

    public static final String EXTRA_MEMBERS_LIST_TYPE = "membersListType";

    public static final int TYPE_PUBLIC_TOPIC = 0x00;
    public static final int TYPE_DIRECT_MESSAGE = 0x01;
    public static final int TYPE_PRIVATE_TOPIC = 0x02;

    public static final int TYPE_FILE_DETAIL_REFRESH = 0x03;

    public static final int NOTIFICATION_ID = 100;
    public static final int NETWORK_SUCCESS = 200;
    public static final String UNVAILABLE_CLIENT_CONNECTION = "unvailable_client_connection";

    public static class RoomType {
        public static final String TYPE_USER = "users";
        public static final String TYPE_PUBLIC = "channels";
        public static final String TYPE_PRIVATE = "privateGroups";
    }

    public static class NetworkError {
        public static final int UNAUTHORIZED = 401;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int DATA_NOT_FOUND = 1839;
        public static final int DUPLICATED_NAME = 400;
        public static final int BAD_REQUEST = 400;
    }

}
