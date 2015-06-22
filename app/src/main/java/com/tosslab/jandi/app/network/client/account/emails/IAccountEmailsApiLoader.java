package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountEmailsApiLoader {

    public IExecutor setExecutorRequestAddEmail(ReqAccountEmail reqAccountEmail);

    public IExecutor setExecutorConfirmEmail(ReqConfirmEmail reqConfirmEmail);

    public IExecutor setExecutorDeleteEmail(ReqAccountEmail reqConfirmEmail);

    public IExecutor setExecutorChangePassword(ReqChangePassword reqConfirmEmail);

}
