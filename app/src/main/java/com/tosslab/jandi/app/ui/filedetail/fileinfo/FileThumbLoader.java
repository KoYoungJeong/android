package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public interface FileThumbLoader {
    void loadThumb(ResMessages.FileMessage fileMessage, int entityId);
}
