package com.tosslab.toss.app.network;

import com.tosslab.toss.app.network.entities.TossRestInfosForSideMenu;
import com.tosslab.toss.app.network.entities.TossRestLogin;
import com.tosslab.toss.app.network.entities.TossRestToken;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@Rest(rootUrl = "https://192.168.0.11:3000/inner-api", converters = { MappingJacksonHttpMessageConverter.class }, interceptors = { LoggerInterceptor.class })
@Accept(MediaType.APPLICATION_JSON)
public interface TossRestClient {
    void setHeader(String name, String value);

    // 로그인
    @Post("/token")
    TossRestToken loginAndReturnToken(TossRestLogin login);

    // 채널, PG, DM 리스트 획득
    @Get("/leftSideMenu")
    @RequiresHeader("Authorization")
    TossRestInfosForSideMenu getInfosForSideMenu();

}
