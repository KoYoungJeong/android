package com.tosslab.jandi.app.network.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 9. 19..
 */
public class ReqInvitation {
    public List<InviteMember> inviteMembers;
    public boolean isDisplayFirstNameFirst;

    public ReqInvitation(String email) {
        inviteMembers = new ArrayList<InviteMember>();
        inviteMembers.add(new InviteMember(email));
        isDisplayFirstNameFirst = false;
    }

    public class InviteMember {
        public String email;

        public InviteMember(String email) {
            this.email = email;
        }
    }
}
