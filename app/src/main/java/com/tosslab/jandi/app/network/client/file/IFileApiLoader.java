package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by tee on 15. 7. 2..
 */
public interface IFileApiLoader {

    IExecutor<ResCommon> loaderDeleteFileByFileApi(int teamId, int fileId);

    IExecutor<List<ResMessages.FileMessage>> loaderSearchInitImageFileByFileApi(int teamId, int roomId
            , int messageId, int count);

    IExecutor<List<ResMessages.FileMessage>> loaderSearchOldImageFileByFileApi(int teamId, int roomId
            , int messageId, int count);

    IExecutor<List<ResMessages.FileMessage>> loaderSearchNewImageFileByFileApi(int teamId, int roomId
            , int messageId, int count);


}
