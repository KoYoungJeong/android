package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 28..
 */
public class ReqUnshareMessage {
    public int teamId;
    public int unshareEntity;

    public ReqUnshareMessage(int teamId, int unshareEntity) {
        this.teamId = teamId;
        this.unshareEntity = unshareEntity;
    }
}
