package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStarredEntityApiLoader {

    Executor<ResCommon> loadEnableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId);

    Executor<ResCommon> loadDisableFavoriteByStarredEntityApi(long teamId, long entityId);

}
