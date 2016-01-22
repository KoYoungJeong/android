package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqCreateTopic {
    public long teamId;
    public String name;
    public String description;
    public boolean autoJoin;
}
