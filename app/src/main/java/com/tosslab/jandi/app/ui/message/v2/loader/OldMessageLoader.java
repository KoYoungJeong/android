package com.tosslab.jandi.app.ui.message.v2.loader;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public interface OldMessageLoader {
    ResMessages load(int roomId, int firstItemId);
}
