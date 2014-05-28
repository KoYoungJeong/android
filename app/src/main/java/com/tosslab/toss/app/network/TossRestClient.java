package com.tosslab.toss.app.network;

import com.tosslab.toss.app.network.entities.ReqCreateCdp;
import com.tosslab.toss.app.network.entities.ResSendCdpMessage;
import com.tosslab.toss.app.network.entities.RestFileUploadResponse;
import com.tosslab.toss.app.network.entities.TossRestInfosForSideMenu;
import com.tosslab.toss.app.network.entities.TossRestLogin;
import com.tosslab.toss.app.network.entities.TossRestPgMessages;
import com.tosslab.toss.app.network.entities.TossRestSendingMessage;
import com.tosslab.toss.app.network.entities.TossRestToken;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.MultiValueMap;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@Rest(
        rootUrl = "https://192.168.0.11:3000/inner-api",
        converters = {
                MappingJacksonHttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class },
        interceptors = { LoggerInterceptor.class }
)
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

    /************************************************************
     * 채널 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // 채널에서 Message 리스트 정보 획득
    @Get("/channel/{channelId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    TossRestPgMessages getChannelMessages(int channelId, int fromId, int numOfPost);

    // 채널에서 Message 생성
    @Post("/channel/{channelId}/message")
    @RequiresHeader("Authorization")
    ResSendCdpMessage sendChannelMessage(TossRestSendingMessage message, int channelId);

    // 채널 생성
    @Post("/channel")
    @RequiresHeader("Authorization")
    ResSendCdpMessage createChannel(ReqCreateCdp channel);


    /************************************************************
     * PG 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    TossRestPgMessages getGroupMessages(int groupId, int fromId, int numOfPost);

    // Private Group에서의 Message 생성
    @Post("/privateGroups/{groupId}/message")
    @RequiresHeader("Authorization")
    ResSendCdpMessage sendGroupMessage(TossRestSendingMessage message, int groupId);

    // Private Group 생성
    @Post("/privateGroup")
    @RequiresHeader("Authorization")
    ResSendCdpMessage createPrivateGroup(ReqCreateCdp group);

    // File Upload
    @Post("/file")
    @RequiresHeader("Authorization")
    RestFileUploadResponse uploadFile(MultiValueMap data);


}
