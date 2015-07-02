package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IAccountProfileApiAuth {

    ResAccountInfo changeNameByAccountProfileApi(ReqProfileName reqProfileName) throws RetrofitError;

    ResAccountInfo changePrimaryEmailByAccountProfileApi(ReqAccountEmail reqAccountEmail) throws RetrofitError;

}
