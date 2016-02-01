package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 7. 2..
 */
@AuthorizedHeader
public interface FileApiV2Client {

    @DELETEWithBody("/files/{fileId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteFile(@Query("teamId") long teamId, @Path("fileId") long fileId);

    @GET("/teams/{teamId}/rooms/{roomId}/images")
    List<ResMessages.FileMessage> searchInitImageFile(@Path("teamId") long teamId
            , @Path("roomId") long roomId
            , @Query("messageId") long messageId
            , @Query("count") int count);

    @GET("/teams/{teamId}/rooms/{roomId}/images?type=old")
    List<ResMessages.FileMessage> searchOldImageFile(@Path("teamId") long teamId
            , @Path("roomId") long roomId
            , @Query("messageId") long messageId
            , @Query("count") int count);

    @GET("/teams/{teamId}/rooms/{roomId}/images?type=new")
    List<ResMessages.FileMessage> searchNewImageFile(@Path("teamId") long teamId
            , @Path("roomId") long roomId
            , @Query("messageId") long messageId
            , @Query("count") int count);

}
