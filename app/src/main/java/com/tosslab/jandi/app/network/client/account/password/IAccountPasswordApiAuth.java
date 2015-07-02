package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountPasswordApiAuth {

    ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError;

}
