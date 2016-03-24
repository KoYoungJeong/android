package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

public interface ValidationApiLoader {

    Executor<ResValidation> loadValidDomain(String domain);
}
