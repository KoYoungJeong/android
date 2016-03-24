package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IStarredEntityApiAuth {

    ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, long entityId) throws IOException;

    ResCommon disableFavoriteByStarredEntityApi(long teamId, long entityId) throws IOException;

}
