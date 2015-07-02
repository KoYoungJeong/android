package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IAccountPasswordApiSimple {

    ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) throws RetrofitError;

}
