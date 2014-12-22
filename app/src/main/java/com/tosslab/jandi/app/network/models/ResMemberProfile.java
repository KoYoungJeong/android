package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 22..
 * Member Profile Detail Info
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMemberProfile {

    public int id;
    public int teamId;
    public String name;
    public String u_email;
    public String u_authority;
    public String u_nickname;
    public String u_tutoredAt;
    public String accountId;
    public Date updatedAt;
    public Date createdAt;
    public String u_statusMessage;
    public List<ResLeftSideMenu.MessageMarker> u_messageMarkers;
    public List<Integer> u_joinEntities;
    public List<Integer> u_starredMessages;
    public List<Integer> u_starredEntities;
    public ResLeftSideMenu.ExtraData u_extraData;
    public ResMessages.ThumbnailUrls u_photoThumbnailUrl;
    public String u_photoUrl;
    public String status;
    public String type;


}
