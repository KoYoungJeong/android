package com.tosslab.jandi.app.network.client.teams.folder;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResUpdateFolder;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class FolderApi extends ApiTemplate<FolderApi.Api> {
    public FolderApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCreateFolder createFolder(long teamId, ReqCreateFolder reqCreateFolder) throws RetrofitException {
        return call(() -> getApi().createFolder(teamId, reqCreateFolder));
    }

    public ResCommon deleteFolder(long teamId, long folderId) throws RetrofitException {
        return call(() -> getApi().deleteFolder(teamId, folderId));
    }

    public ResUpdateFolder updateFolder(long teamId, long folderId,
                                 ReqUpdateFolder reqUpdateFolder) throws RetrofitException {
        return call(() -> getApi().updateFolder(teamId, folderId, reqUpdateFolder));
    }

    public List<ResFolder> getFolders(long teamId) throws RetrofitException {
        return call(() -> getApi().getFolders(teamId));
    }

    public List<ResFolderItem> getFolderItems(long teamId) throws RetrofitException {
        return call(() -> getApi().getFolderItems(teamId));
    }

    public ResRegistFolderItem registFolderItem(long teamId, long folderId,
                                         ReqRegistFolderItem reqRegistFolderItem) throws RetrofitException {
        return call(() -> getApi().registFolderItem(teamId, folderId, reqRegistFolderItem));
    }

    public ResCommon deleteFolderItem(long teamId, long folderId,
                               long itemId) throws RetrofitException {
        return call(() -> getApi().deleteFolderItem(teamId, folderId, itemId));
    }

    interface Api {
        @POST("teams/{teamId}/folders")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCreateFolder> createFolder(@Path("teamId") long teamId, @Body ReqCreateFolder reqCreateFolder);

        @DELETE("teams/{teamId}/folders/{folderId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteFolder(@Path("teamId") long teamId, @Path("folderId") long folderId);

        @PUT("teams/{teamId}/folders/{folderId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResUpdateFolder> updateFolder(@Path("teamId") long teamId, @Path("folderId") long folderId,
                                           @Body ReqUpdateFolder reqUpdateFolder);

        @GET("teams/{teamId}/folders")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResFolder>> getFolders(@Path("teamId") long teamId);

        @GET("teams/{teamId}/folders/items")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResFolderItem>> getFolderItems(@Path("teamId") long teamId);

        @POST("teams/{teamId}/folders/{folderId}/items")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResRegistFolderItem> registFolderItem(@Path("teamId") long teamId, @Path("folderId") long folderId,
                                                   @Body ReqRegistFolderItem reqRegistFolderItem);

        @DELETE("teams/{teamId}/folders/{folderId}/items/{itemId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteFolderItem(@Path("teamId") long teamId, @Path("folderId") long folderId,
                                         @Path("itemId") long itemId);
    }
}
