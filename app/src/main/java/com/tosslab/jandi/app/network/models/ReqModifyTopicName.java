package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqModifyTopicName {
    public int teamId;
    public String name;
}
