package com.tosslab.jandi.app.network.client.platform;

import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 7. 31..
 */
public interface IPlatformApiAuth {

    ResCommon updatePlatformStatus(ReqUpdatePlatformStatus reqUpdatePlatformStatus) throws RetrofitError;

}
