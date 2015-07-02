package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStarredEntityApiAuth {

    ResCommon enableFavoriteByStarredEntityApi(ReqTeam reqTeam, int entityId) throws RetrofitError;

    ResCommon disableFavoriteByStarredEntityApi(int teamId, int entityId) throws RetrofitError;

}
