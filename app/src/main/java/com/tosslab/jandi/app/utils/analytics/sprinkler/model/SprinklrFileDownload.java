package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrFileDownload extends MainSprinklrModel
        implements IFileId {

    private SprinklrFileDownload() {
        super(SprinklerEvents.FileDownload, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrFileDownload().sendFail(errorCode);
    }

    public static void sendLog(long fileId) {
        new SprinklrFileDownload().setFileId(fileId).sendSuccess();
    }

    @Override
    public MainSprinklrModel setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        return this;
    }

}
