package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.start.Human;

import java.io.File;
import java.net.URLConnection;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class ProfileApi extends ApiTemplate<ProfileApi.Api> {
    @Inject
    public ProfileApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public Human updateMemberProfile(long teamId, long memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitException {
        return call(() -> getApi().updateMemberProfile(teamId, memberId, reqUpdateProfile));
    }

    public ResAvatarsInfo getAvartarsInfo() throws RetrofitException {
        return call(() -> getApi().getAvartarsInfo());
    }

    public Human getMemberProfile(long teamId, long memberId) throws RetrofitException {
        return call(() -> getApi().getMemberProfile(teamId, memberId));
    }

    public Human uploadProfilePhoto(long teamId, long memberId, File file) throws RetrofitException {
        MediaType mediaType = MediaType.parse(URLConnection.guessContentTypeFromName(file.getName()));
        MultipartBody.Part userFilePart = MultipartBody.Part.createFormData("photo", file.getName(), RequestBody.create(mediaType, file));
        return call(() -> getApi().uploadProfilePhoto(teamId, memberId, userFilePart));
    }


    interface Api {

        @GET("teams/{teamId}/members/{memberId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<Human> getMemberProfile(@Path("teamId") long teamId,
                                     @Path("memberId") long memberId);

        @PUT("teams/{teamId}/members/{memberId}/profile")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<Human> updateMemberProfile(@Path("teamId") long teamId,
                                        @Path("memberId") long memberId,
                                        @Body ReqUpdateProfile reqUpdateProfile);

        @GET("avatars")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAvatarsInfo> getAvartarsInfo();

        @Multipart
        @PUT("teams/{teamId}/members/{memberId}/profile")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<Human> uploadProfilePhoto(@Path("teamId") long teamId,
                                       @Path("memberId") long memberId,
                                       @Part MultipartBody.Part photo);
    }
}
