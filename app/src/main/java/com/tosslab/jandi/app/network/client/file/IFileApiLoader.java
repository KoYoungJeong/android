package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by tee on 15. 7. 2..
 */
public interface IFileApiLoader {

    Executor<ResCommon> loaderDeleteFileByFileApi(long teamId, long fileId);

    Executor<List<ResMessages.FileMessage>> loaderSearchInitImageFileByFileApi(long teamId, long roomId
            , long messageId, int count);

    Executor<List<ResMessages.FileMessage>> loaderSearchOldImageFileByFileApi(long teamId, long roomId
            , long messageId, int count);

    Executor<List<ResMessages.FileMessage>> loaderSearchNewImageFileByFileApi(long teamId, long roomId
            , long messageId, int count);


}
