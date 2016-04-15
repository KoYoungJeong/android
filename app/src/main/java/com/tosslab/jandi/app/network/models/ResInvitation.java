package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 9. 19..
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResInvitation {
    public int inviteTotal;
    public int sendMailSuccessCount;
    public int sendMailFailCount;
    public List<String> sendMailFailList;
}
