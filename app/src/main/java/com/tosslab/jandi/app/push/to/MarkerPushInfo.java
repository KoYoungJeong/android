package com.tosslab.jandi.app.push.to;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MarkerPushInfo extends BasePushInfo {
    @JsonProperty("link_id")
    private long linkId;

    public MarkerPushInfo() {
        setPushType("marker_updated");
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

}
