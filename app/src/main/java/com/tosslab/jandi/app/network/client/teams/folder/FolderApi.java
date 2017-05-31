package com.tosslab.jandi.app.network.client.teams.folder;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateFolder;
import com.tosslab.jandi.app.network.models.ReqRegistFolderItem;
import com.tosslab.jandi.app.network.models.ReqUpdateFolder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResCreateFolder;
import com.tosslab.jandi.app.network.models.ResRegistFolderItem;
import com.tosslab.jandi.app.network.models.ResUpdateFolder;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class FolderApi extends ApiTemplate<FolderApi.Api> {
    @Inject
    public FolderApi(InnerApiRetrofitBuilder retrofitBuilder) {
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
