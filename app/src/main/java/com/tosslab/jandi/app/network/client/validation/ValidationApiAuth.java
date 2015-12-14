package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.network.models.validation.ResValidation;

public interface ValidationApiAuth {
    ResValidation validDomain(String domain);
}
