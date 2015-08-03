package com.tosslab.jandi.app.network.client.platform;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.PUT;

/**
 * Created by tonyjs on 15. 7. 31..
 */
@AuthorizedHeader
public interface PlatformApiV2Client {

    @PUT("/platform/active")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon updatePlatformStatus(@Body ReqUpdatePlatformStatus reqUpdatePlatformStatus);

}
