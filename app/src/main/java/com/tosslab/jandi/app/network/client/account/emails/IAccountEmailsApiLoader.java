package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountEmailsApiLoader {

    IExecutor<ResAccountInfo> loadRequestAddEmailByAccountEmailsApi(ReqAccountEmail reqAccountEmail);

    IExecutor<ResAccountInfo> loadConfirmEmailByAccountEmailsApi(ReqConfirmEmail reqConfirmEmail);

    IExecutor<ResAccountInfo> loadExecutorDeleteEmailByAccountEmailsApi(ReqAccountEmail reqConfirmEmail);

    IExecutor<ResCommon> loadChangePasswordByAccountEmailsApi(ReqChangePassword reqConfirmEmail);

}
