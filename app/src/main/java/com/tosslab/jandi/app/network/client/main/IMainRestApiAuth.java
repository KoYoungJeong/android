package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResSearchFile;



/**
 * Created by tee on 15. 6. 20..
 */
public interface IMainRestApiAuth {

    ResAccountInfo getAccountInfoByMainRest() throws IOException;

    ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws IOException;

    ResLeftSideMenu getInfosForSideMenuByMainRest(long teamId) throws IOException;

    ResCommon setMarkerByMainRest(long entityId, ReqSetMarker reqSetMarker) throws IOException;

    ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws IOException;

}
