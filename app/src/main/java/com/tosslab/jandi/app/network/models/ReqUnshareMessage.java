package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 28..
 */
public class ReqUnshareMessage {
    public long teamId;
    public long unshareEntity;

    public ReqUnshareMessage(long teamId, long unshareEntity) {
        this.teamId = teamId;
        this.unshareEntity = unshareEntity;
    }
}
