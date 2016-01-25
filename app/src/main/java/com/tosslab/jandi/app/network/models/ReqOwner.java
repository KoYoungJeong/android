package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 16. 1. 11..
 */
public class ReqOwner {
    private long adminId;

    public ReqOwner(long adminId) {
        this.adminId = adminId;
    }

    public long getAdminId() {
        return adminId;
    }
}
