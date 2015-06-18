package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@Rest(
        rootUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api",
        converters = {
                JandiV2HttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class},
        interceptors = {LoggerInterceptor.class}
)

@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface JandiRestClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);

    // 클라이언트 Policy(+version) 정보
    @Get("/config")
    ResConfig getConfig();

    // 내 팀 정보 획득
    @Get("/info/teamlist/email/{userEmail}")
    ResMyTeam getTeamId(String userEmail);

    // 로그인
    @Post("/token")
    @RequiresHeader("Content-Type")
    ResAccessToken getAccessToken(ReqAccessToken login);

    @Get("/account")
    @RequiresAuthentication
    ResAccountInfo getAccountInfo();

    @Post("/accounts")
    ResAccountInfo signUpAccount(ReqSignUpInfo signUpInfo);

    @Put("/account")
    @RequiresAuthentication
    ResAccountInfo updatePrimaryEmail(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo);

    @Post("/accounts/activate")
    @RequiresAuthentication
    ResAccountInfo activateAccount(ReqAccountActivate reqAccountActivate);

    // 채널, PG, DM 리스트 획득
    @Get("/leftSideMenu?teamId={teamId}")
    @RequiresAuthentication
    ResLeftSideMenu getInfosForSideMenu(int teamId);

    // Entity별 badge 설정
    @Post("/entities/{entityId}/marker")
    @RequiresAuthentication
    ResCommon setMarker(int entityId, ReqSetMarker reqSetMarker);

    /**
     * *********************************************************
     * Search
     * **********************************************************
     */
    // File search
    @Post("/search")
    @RequiresAuthentication
    ResSearchFile searchFile(ReqSearchFile reqSearchFile);

}
