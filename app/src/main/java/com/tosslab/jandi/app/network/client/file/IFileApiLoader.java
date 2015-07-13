package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 7. 2..
 */
public interface IFileApiLoader {

    IExecutor<ResCommon> loaderDeleteFileByFileApi(int teamId, int fileId);

}
