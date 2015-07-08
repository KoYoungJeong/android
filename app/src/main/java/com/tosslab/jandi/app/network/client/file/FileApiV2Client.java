package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ResCommon;

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
    ResCommon deleteFile(@Query("teamId") int teamId, @Path("fileId") int fileId);

}
