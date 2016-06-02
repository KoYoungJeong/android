package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.services.socket.annotations.Version;
import com.tosslab.jandi.app.team.member.User;

/**
 * Created by Steve SeongUg Jung on 15. 5. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketMemberProfileEvent {
    private int version;
    private User member;
    private long ts;

    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }


    @Override
    public String toString() {
        return "SocketMemberProfileEvent{" +
                "version=" + version +
                ", member=" + member +
                '}';
    }
}
