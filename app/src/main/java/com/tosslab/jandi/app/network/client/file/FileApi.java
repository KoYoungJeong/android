package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class FileApi extends ApiTemplate<FileApi.Api> {

    public FileApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon deleteFile(long fileId, long teamId) throws RetrofitException {
        return call(() -> getApi().deleteFile(fileId, teamId));
    }

    public List<ResMessages.FileMessage> searchInitImageFile(long teamId
            , long roomId, long messageId, int count) throws RetrofitException {
        return call(() -> getApi().searchInitImageFile(teamId, roomId, messageId, count));
    }

    public List<ResMessages.FileMessage> searchOldImageFile(long teamId
            , long roomId, long messageId, int count) throws RetrofitException {
        return call(() -> getApi().searchOldImageFile(teamId, roomId, messageId, count));
    }

    public List<ResMessages.FileMessage> searchNewImageFile(long teamId
            , long roomId, long messageId, int count) throws RetrofitException {
        return call(() -> getApi().searchNewImageFile(teamId, roomId, messageId, count));
    }

    public ResMessages.FileMessage enableFileExternalLink(long teamId, long fileId, ReqNull reqNull) throws RetrofitException {
        return call(() -> getApi().enableFileExternalLink(teamId, fileId, reqNull));
    }

    public ResMessages.FileMessage disableFileExternalLink(long teamId, long fileId) throws RetrofitException {
        return call(() -> getApi().disableFileExternalLink(teamId, fileId));
    }

    interface Api {

        @HTTP(path = "files/{fileId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteFile(@Path("fileId") long fileId, @Query("teamId") long teamId);

        @GET("teams/{teamId}/rooms/{roomId}/images")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResMessages.FileMessage>> searchInitImageFile(@Path("teamId") long teamId,
                                                                @Path("roomId") long roomId,
                                                                @Query("messageId") long messageId,
                                                                @Query("count") int count);

        @GET("teams/{teamId}/rooms/{roomId}/images?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResMessages.FileMessage>> searchOldImageFile(@Path("teamId") long teamId,
                                                               @Path("roomId") long roomId,
                                                               @Query("messageId") long messageId,
                                                               @Query("count") int count);

        @GET("teams/{teamId}/rooms/{roomId}/images?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResMessages.FileMessage>> searchNewImageFile(@Path("teamId") long teamId,
                                                               @Path("roomId") long roomId,
                                                               @Query("messageId") long messageId,
                                                               @Query("count") int count);

        @PUT("teams/{teamId}/files/{fileId}/externalShared")
        Call<ResMessages.FileMessage> enableFileExternalLink(@Path("teamId") long teamId,
                                                             @Path("fileId") long fileId,
                                                             @Body ReqNull reqNull);

        @DELETE("teams/{teamId}/files/{fileId}/externalShared")
        Call<ResMessages.FileMessage> disableFileExternalLink(@Path("teamId") long teamId,
                                                              @Path("fileId") long fileId);
    }
}
