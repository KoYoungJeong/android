package com.tosslab.toss.app;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class TossConstants {
    public static final String SERVICE_ROOT_URL = "https://112.219.215.146:3000/";

    public static final int TYPE_CHANNEL        = 0x00;
    public static final int TYPE_DIRECT_MESSAGE = 0x01;
    public static final int TYPE_PRIVATE_GROUP  = 0x02;
    public static final int TYPE_TITLE_JOINED_CHANNEL   = 0x10;
    public static final int TYPE_TITLE_UNJOINED_CHANNEL = 0x11;
    public static final int TYPE_TITLE_DIRECT_MESSAGE   = 0x12;
    public static final int TYPE_TITLE_PRIVATE_GROUP    = 0x13;

    public static final String TYPE_CHANNEL_STRING = "channel";
    public static final String TYPE_DIRECT_MESSAGE_STRING = "user";
    public static final String TYPE_PRIVATE_GROUP_STRING = "parivateGroup";
}
