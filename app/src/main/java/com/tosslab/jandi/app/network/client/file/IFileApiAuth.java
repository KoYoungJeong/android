package com.tosslab.jandi.app.network.client.file;

import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by tee on 15. 7. 2..
 */
public interface IFileApiAuth {

    ResCommon deleteFileByFileApi(long teamId, long fileId);

    List<ResMessages.FileMessage> searchInitImageFileByFileApi(int teamId, int roomId
            , int messageId, int count);

    List<ResMessages.FileMessage> searchOldImageFileByFileApi(int teamId, int roomId
            , int messageId, int count);

    List<ResMessages.FileMessage> searchNewImageFileByFileApi(int teamId, int roomId
            , int messageId, int count);

}
