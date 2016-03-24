package com.tosslab.jandi.app.network.client.platform;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tonyjs on 15. 7. 31..
 */
public interface IPlatformApiLoader {
    Executor<ResCommon> loadUpdatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus);
}
