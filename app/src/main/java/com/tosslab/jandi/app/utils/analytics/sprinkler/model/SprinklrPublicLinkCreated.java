package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrPublicLinkCreated extends MainSprinklrModel
        implements IFileId {

    private SprinklrPublicLinkCreated() {
        super(SprinklerEvents.PublicLinkCreated, true, true);
    }

    public static void sendLog(long fileId) {
        new SprinklrPublicLinkCreated()
                .setFileId(fileId)
                .sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrPublicLinkCreated().sendFail(errorCode);
    }

    @Override
    public MainSprinklrModel setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        return this;
    }

}
