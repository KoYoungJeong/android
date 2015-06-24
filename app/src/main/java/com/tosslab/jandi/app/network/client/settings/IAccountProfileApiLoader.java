package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IAccountProfileApiLoader {

    IExecutor loadChangeNameByAccountProfileApi(ReqProfileName reqProfileName);

    IExecutor loadChangePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail);

}
