package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface ProfileApiV2Client {

    @PUT("/members/{memberId}/profile")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResLeftSideMenu.User updateMemberProfile(@Path("memberId") int memberId, @Body ReqUpdateProfile reqUpdateProfile);

    @PUT("/members/{memberId}/name")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon updateMemberName(@Path("memberId") int memberId, @Body ReqProfileName reqProfileName);

    @PUT("/members/{memberId}/email")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResLeftSideMenu.User updateMemberEmail(@Path("memberId") int memberId, @Body ReqAccountEmail reqAccountEmail);

    @GET("/avatars")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAvatarsInfo getAvartarsInfo();

}
