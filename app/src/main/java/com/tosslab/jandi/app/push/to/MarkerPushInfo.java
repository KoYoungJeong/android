package com.tosslab.jandi.app.push.to;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MarkerPushInfo extends BasePushInfo {

    public MarkerPushInfo() {
        setPushType("marker_updated");
    }

}
