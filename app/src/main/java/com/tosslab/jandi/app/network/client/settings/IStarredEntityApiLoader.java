package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqTeam;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStarredEntityApiLoader {

    IExecutor loadEnableFavoriteByStarredEntityApi(ReqTeam reqTeam, int entityId);

    IExecutor loadDisableFavoriteByStarredEntityApi(int teamId, int entityId);

}
