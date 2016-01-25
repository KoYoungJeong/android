package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IMainRestApiAuth {

    ResAccountInfo getAccountInfoByMainRest() throws RetrofitError;

    ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError;

    ResLeftSideMenu getInfosForSideMenuByMainRest(long teamId) throws RetrofitError;

    ResCommon setMarkerByMainRest(long entityId, ReqSetMarker reqSetMarker) throws RetrofitError;

    ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError;

}
