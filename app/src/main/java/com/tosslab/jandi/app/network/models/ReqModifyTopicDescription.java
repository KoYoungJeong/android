package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqModifyTopicDescription {
    public long teamId;
    public String description;
}
