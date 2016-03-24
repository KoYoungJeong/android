package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IAccountProfileApiLoader {

    Executor<ResAccountInfo> loadChangeNameByAccountProfileApi(ReqProfileName reqProfileName);

    Executor<ResAccountInfo> loadChangePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail);

}
