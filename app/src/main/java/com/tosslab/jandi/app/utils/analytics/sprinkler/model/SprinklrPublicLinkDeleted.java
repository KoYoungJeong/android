package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrPublicLinkDeleted extends MainSprinklrModel
        implements IFileId {

    private SprinklrPublicLinkDeleted() {
        super(SprinklerEvents.PublicLinkDeleted, true, true);
    }

    public static void sendLog(long fileId) {
        new SprinklrPublicLinkDeleted()
                .setFileId(fileId)
                .sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrPublicLinkDeleted().sendFail(errorCode);
    }

    @Override
    public MainSprinklrModel setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        return this;
    }

}
