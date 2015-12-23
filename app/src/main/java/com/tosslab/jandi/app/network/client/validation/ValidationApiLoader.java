package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

public interface ValidationApiLoader {

    IExecutor<ResValidation> loadValidDomain(String domain);
}
