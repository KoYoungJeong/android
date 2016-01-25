package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 18..
 */
public class ReqNotificationTarget {
    public static final String TARGET_ALL = "all";
    public static final String TARGET_DM_ONLY = "dmOnly";
    public static final String TARGET_NONE = "none";

    public String subscriptionTarget;

    public ReqNotificationTarget(String subscriptionTarget) {
        this.subscriptionTarget = subscriptionTarget;
    }
}
