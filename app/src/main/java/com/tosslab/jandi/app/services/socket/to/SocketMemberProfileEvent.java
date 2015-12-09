package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 5. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketMemberProfileEvent {
    private int version;
    private ResLeftSideMenu.User member;

    public ResLeftSideMenu.User getMember() {
        return member;
    }

    public void setMember(ResLeftSideMenu.User member) {
        this.member = member;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SocketMemberProfileEvent{" +
                "version=" + version +
                ", member=" + member +
                '}';
    }
}
