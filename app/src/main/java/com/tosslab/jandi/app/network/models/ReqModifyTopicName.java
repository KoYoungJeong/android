package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqModifyTopicName {
    public long teamId;
    public String name;
}
