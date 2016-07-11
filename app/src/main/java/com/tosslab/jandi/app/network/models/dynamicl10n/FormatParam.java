package com.tosslab.jandi.app.network.models.dynamicl10n;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class FormatParam {
    private String typOf;

    public String getTypOf() {
        return typOf;
    }

    public void setTypOf(String typOf) {
        this.typOf = typOf;
    }
}
