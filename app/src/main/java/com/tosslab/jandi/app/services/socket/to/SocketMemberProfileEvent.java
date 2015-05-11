package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 5. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketMemberProfileEvent {
    private ResLeftSideMenu.User member;

    public ResLeftSideMenu.User getMember() {
        return member;
    }

    public void setMember(ResLeftSideMenu.User member) {
        this.member = member;
    }
}
