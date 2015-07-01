package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountPasswordApiLoader {

    IExecutor<ResCommon> loadChangePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail);

    IExecutor<ResCommon> loadResetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail);

}
