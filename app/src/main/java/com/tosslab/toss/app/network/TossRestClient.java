package com.tosslab.toss.app.network;

import com.tosslab.toss.app.TossConstants;
import com.tosslab.toss.app.network.entities.ReqCreateCdp;
import com.tosslab.toss.app.network.entities.ReqModifyMessage;
import com.tosslab.toss.app.network.entities.ReqSendMessage;
import com.tosslab.toss.app.network.entities.ResChannelMessagesUpdated;
import com.tosslab.toss.app.network.entities.ResDirectMessagesUpdated;
import com.tosslab.toss.app.network.entities.ResLeftSideMenu;
import com.tosslab.toss.app.network.entities.ResLogin;
import com.tosslab.toss.app.network.entities.ResMessages;
import com.tosslab.toss.app.network.entities.ResPrivateGroupMessagesUpdated;
import com.tosslab.toss.app.network.entities.ResSendMessage;
import com.tosslab.toss.app.network.entities.RestFileUploadResponse;
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

import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@Rest(
        rootUrl = TossConstants.SERVICE_ROOT_URL + "inner-api",
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
    TossRestToken loginAndReturnToken(ResLogin login);

    // 채널, PG, DM 리스트 획득
    @Get("/leftSideMenu")
    @RequiresHeader("Authorization")
    ResLeftSideMenu getInfosForSideMenu();

    /************************************************************
     * 채널 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // 채널 생성
    @Post("/channel")
    @RequiresHeader("Authorization")
    ResSendMessage createChannel(ReqCreateCdp channel);

    // 채널 수정
    @Put("/channels/{channelId}")
    @RequiresHeader("Authorization")
    ResSendMessage modifyChannel(ReqCreateCdp channel, int channelId);

    // 채널 삭제
    @Delete("/channels/{channelId}")
    @RequiresHeader("Authorization")
    ResSendMessage deleteChannel(int channelId);

    // 채널에서 Message 리스트 정보 획득
    @Get("/channels/{channelId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResMessages getChannelMessages(int channelId, int fromId, int numOfPost);

    @Get("/channels/{channelId}/messages/update/{timeAfter}")
    @RequiresHeader("Authorization")
    ResChannelMessagesUpdated getChannelMessagesUpdated(int channelId, Date timeAfter);

    // 채널에서 Message 생성
    @Post("/channels/{channelId}/message")
    @RequiresHeader("Authorization")
    ResSendMessage sendChannelMessage(ReqSendMessage message, int channelId);

    // 채널에서 Message 수정
    @Put("/channels/{channelId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendMessage modifyChannelMessage(ReqModifyMessage message,
                                           int channelId, int messageId);

    // 채널에서 Message 삭제
    @Delete("/channels/{channelId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendMessage deleteChannelMessage(int channelId, int messageId);


    /************************************************************
     * Direct Message 관련
     * 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResMessages getDirectMessages(int userId, int fromId, int numOfPost);

    // Updated 된 Direct Message 리스트 정보 획득
    @Get("/users/{userId}/messages/update/{timeAfter}")
    @RequiresHeader("Authorization")
    ResDirectMessagesUpdated getDirectMessagesUpdated(int userId, Date timeAfter);

    // Direct Message 생성
    @Post("/users/{userId}/message")
    @RequiresHeader("Authorization")
    ResSendMessage sendDirectMessage(ReqSendMessage message, int userId);

    // Direct Message 수정
    @Put("/users/{userId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendMessage modifyDirectMessage(ReqModifyMessage message,
                                          int userId, int messageId);

    // Direct Message 삭제
    @Delete("/users/{userId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendMessage deleteDirectMessage(int userId, int messageId);


    /************************************************************
     * PG 관련
     * 생성 / 수정 / 삭제, 내부 메시지 생성 / 수정 / 삭제
     ************************************************************/
    // Private Group 생성
    @Post("/privateGroup")
    @RequiresHeader("Authorization")
    ResSendMessage createPrivateGroup(ReqCreateCdp group);

    // 채널 수정
    @Put("/privateGroups/{groupId}")
    @RequiresHeader("Authorization")
    ResSendMessage modifyGroup(ReqCreateCdp channel, int groupId);

    // Private Group 삭제
    @Delete("/privateGroups/{groupId}")
    @RequiresHeader("Authorization")
    ResSendMessage deleteGroup(int groupId);

    // Private Group의 Message 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/{fromId}/{numOfPost}")
    @RequiresHeader("Authorization")
    ResMessages getGroupMessages(int groupId, int fromId, int numOfPost);

    // Updated 된 Private Group의 리스트 정보 획득
    @Get("/privateGroups/{groupId}/messages/update/{timeAfter}")
    @RequiresHeader("Authorization")
    ResPrivateGroupMessagesUpdated getGroupMessagesUpdated(int groupId, Date timeAfter);

    // Private Group에서의 Message 생성
    @Post("/privateGroups/{groupId}/message")
    @RequiresHeader("Authorization")
    ResSendMessage sendGroupMessage(ReqSendMessage message, int groupId);

    // Private Group Message 수정
    @Put("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendMessage modifyPrivateGroupMessage(ReqModifyMessage message,
                                           int groupId, int messageId);

    // Private Group Message 삭제
    @Delete("/privateGroups/{groupId}/messages/{messageId}")
    @RequiresHeader("Authorization")
    ResSendMessage deletePrivateGroupMessage(int groupId, int messageId);

    /************************************************************
     * File upload
     ************************************************************/
    // File Upload
    @Post("/file")
    @RequiresHeader("Authorization")
    RestFileUploadResponse uploadFile(MultiValueMap data);


}
