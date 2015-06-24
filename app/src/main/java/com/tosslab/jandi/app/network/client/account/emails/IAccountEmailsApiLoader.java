package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountEmailsApiLoader {

    public IExecutor loadRequestAddEmailByAccountEmailsApi(ReqAccountEmail reqAccountEmail);

    public IExecutor loadConfirmEmailByAccountEmailsApi(ReqConfirmEmail reqConfirmEmail);

    public IExecutor loadExecutorDeleteEmailByAccountEmailsApi(ReqAccountEmail reqConfirmEmail);

    public IExecutor loadChangePasswordByAccountEmailsApi(ReqChangePassword reqConfirmEmail);

}
