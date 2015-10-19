package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface MainRestApiClient {


    // 클라이언트 Policy(+version) 정보
    @GET("/config")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResConfig getConfig();

    // 내 팀 정보 획득
    @GET("/info/teamlist/email/{userEmail}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMyTeam getTeamId(@Body String userEmail);

    // 로그인
    @POST("/token")
    @Headers({"Content-Type : application/json",
            "Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT})
    ResAccessToken getAccessToken(@Body ReqAccessToken login);

    @GET("/account")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo getAccountInfo();

    @POST("/accounts")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V4)
    ResCommon signUpAccount(@Body ReqSignUpInfo signUpInfo);

    @PUT("/account")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo updatePrimaryEmail(@Body ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo);

    @POST("/accounts/activate")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResAccountActivate activateAccount(@Body ReqAccountActivate reqAccountActivate);

    @POST("/accounts/verification")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon accountVerification(@Body ReqAccountVerification reqAccountVerification);

    // 채널, PG, DM 리스트 획득
    @GET("/leftSideMenu")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResLeftSideMenu getInfosForSideMenu(@Query("teamId") int teamId);

    // Entity별 badge 설정
    @POST("/entities/{entityId}/marker")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon setMarker(@Path("entityId") int entityId, @Body ReqSetMarker reqSetMarker);

    /**
     * *********************************************************
     * Search
     * **********************************************************
     */
    // File search
    @POST("/search")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResSearchFile searchFile(@Body ReqSearchFile reqSearchFile);

}
