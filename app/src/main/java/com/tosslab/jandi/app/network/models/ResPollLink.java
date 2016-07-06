package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by tonyjs on 16. 6. 21..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResPollLink {
    private ResMessages.Link linkMessage;

    public ResMessages.Link getLinkMessage() {
        return linkMessage;
    }

    public void setLinkMessage(ResMessages.Link linkMessage) {
        this.linkMessage = linkMessage;
    }

    @Override
    public String toString() {
        return "ResPollLink{" +
                "linkMessage=" + linkMessage +
                '}';
    }
}
