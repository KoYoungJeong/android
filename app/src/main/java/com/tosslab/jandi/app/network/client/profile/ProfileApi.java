package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class ProfileApi extends ApiTemplate<ProfileApi.Api> {
    public ProfileApi() {
        super(Api.class);
    }

    ResLeftSideMenu.User updateMemberProfile(long memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitException {
        return call(() -> getApi().updateMemberProfile(memberId, reqUpdateProfile));
    }

    ResCommon updateMemberName(long memberId, ReqProfileName reqProfileName) throws RetrofitException {
        return call(() -> getApi().updateMemberName(memberId, reqProfileName));
    }

    ResLeftSideMenu.User updateMemberEmail(long memberId, ReqAccountEmail reqAccountEmail) throws RetrofitException {
        return call(() -> getApi().updateMemberEmail(memberId, reqAccountEmail));
    }

    ResAvatarsInfo getAvartarsInfo() throws RetrofitException {
        return call(() -> getApi().getAvartarsInfo());
    }


    interface Api {

        @PUT("/members/{memberId}/profile")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResLeftSideMenu.User> updateMemberProfile(@Path("memberId") long memberId, @Body ReqUpdateProfile reqUpdateProfile);

        @PUT("/members/{memberId}/name")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> updateMemberName(@Path("memberId") long memberId, @Body ReqProfileName reqProfileName);

        @PUT("/members/{memberId}/email")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResLeftSideMenu.User> updateMemberEmail(@Path("memberId") long memberId, @Body ReqAccountEmail reqAccountEmail);

        @GET("/avatars")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAvatarsInfo> getAvartarsInfo();

    }
}
