package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 8. 19..
 */
public class ReqSetMarker {
    public static final String CHANNEL         = "channel";
    public static final String PRIVATEGROUP    = "privateGroup";
    public static final String USER            = "user";

    public int lastLinkId;
    public String entityType;
    public ReqSetMarker(int lastLinkId, String entityType) {
        this.lastLinkId = lastLinkId;
        this.entityType = entityType;
    }
}
