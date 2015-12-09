package com.tosslab.jandi.app.network.models.validation;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResValidation {
    private boolean isValidate;

    public boolean isValidate() {
        return isValidate;
    }

    public void setIsValidate(boolean isValidate) {
        this.isValidate = isValidate;
    }
}
