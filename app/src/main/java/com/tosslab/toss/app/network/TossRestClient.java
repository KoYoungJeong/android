package com.tosslab.toss.app.network;

import com.tosslab.toss.app.navigation.MessageItemView;
import com.tosslab.toss.app.network.entities.ResChannelMessages;
import com.tosslab.toss.app.network.entities.ReqCreateCdp;
import com.tosslab.toss.app.network.entities.ReqModifyCdpMessage;
import com.tosslab.toss.app.network.entities.ResDirectMessages;
import com.tosslab.toss.app.network.entities.ResPrivateGroupMessage;
import com.tosslab.toss.app.network.entities.ResSendCdpMessage;
import com.tosslab.toss.app.network.entities.RestFileUploadResponse;
import com.tosslab.toss.app.network.entities.TossRestInfosForSideMenu;
import com.tosslab.toss.app.network.entities.TossRestLogin;
import com.tosslab.toss.app.network.entities.ReqSendCdpMessage;
import com.tosslab.toss.app.network.entities.TossRestToken;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
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
//        rootUrl = "https://121.162.244.90:3000/inner-api",
        rootUrl = MessageItemView.sRootUrl + "inner-api",
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
    // 채널 생성
    @Post("/channel")
    @RequiresHeader("Authorization")
    ResSendCdpMessage createChannel(ReqCreateCdp channel);

    // 채널 수정
    @Put("/channels/{channelId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage modifyChannel(ReqCreateCdp channel, int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage deleteChannel(int channelId);

    // 채널에서 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResChannelMessages getChannelMessages(int channelId, int fromId, int numOfPost);

    // 채널에서 Message 생성
    @Post("/channels/{channelId}/message")
    @RequiresHeader("Authorization")
    ResSendCdpMessage sendChannelMessage(ReqSendCdpMessage message, int channelId);

    // 채널에서 Message 수정
    @Put("/channels/{channelId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage modifyChannelMessage(ReqModifyCdpMessage message,
                                           int channelId, int messageId);

    // 채널에서 Message 삭제
    @Delete("/channels/{channelId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage deleteChannelMessage(int channelId, int messageId);


    /************************************************************
     * Direct Message 관련
     * 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResDirectMessages getDirectMessages(int userId, int fromId, int numOfPost);

    // Direct Message 생성
    @Post("/users/{userId}/message")
    @RequiresHeader("Authorization")
    ResSendCdpMessage sendDirectMessage(ReqSendCdpMessage message, int userId);

    // Direct Message 수정
    @Put("/users/{userId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage modifyDirectMessage(ReqModifyCdpMessage message,
                                           int userId, int messageId);

    // Direct Message 삭제
    @Delete("/users/{userId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage deleteDirectMessage(int userId, int messageId);


    /************************************************************
     * PG 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Private Group 생성
    @Post("/privateGroup")
    @RequiresHeader("Authorization")
    ResSendCdpMessage createPrivateGroup(ReqCreateCdp group);

    // 채널 수정
    @Put("/privateGroups/{groupId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage modifyGroup(ReqCreateCdp channel, int groupId);

    // Private Group 삭제
    @Delete("/privateGroups/{groupId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage deleteGroup(int groupId);

    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResPrivateGroupMessage getGroupMessages(int groupId, int fromId, int numOfPost);

    // Private Group에서의 Message 생성
    @Post("/privateGroups/{groupId}/message")
    @RequiresHeader("Authorization")
    ResSendCdpMessage sendGroupMessage(ReqSendCdpMessage message, int groupId);

    // Private Group Message 수정
    @Put("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage modifyPrivateGroupMessage(ReqModifyCdpMessage message,
                                           int groupId, int messageId);

    // Private Group Message 삭제
    @Delete("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendCdpMessage deletePrivateGroupMessage(int groupId, int messageId);

    /************************************************************
     * File upload
     ************************************************************/
    // File Upload
    @Post("/file")
    @RequiresHeader("Authorization")
    RestFileUploadResponse uploadFile(MultiValueMap data);


}
