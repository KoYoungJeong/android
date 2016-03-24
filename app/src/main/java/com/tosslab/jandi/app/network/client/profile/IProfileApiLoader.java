package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IProfileApiLoader {

    Executor<ResLeftSideMenu.User> loadUpdateMemberProfileByProfileApi(long memberId, ReqUpdateProfile reqUpdateProfile);

    Executor<ResCommon> loadUpdateMemberNameByProfileApi(long memberId, ReqProfileName reqProfileName);

    Executor<ResLeftSideMenu.User> loadUpdateMemberEmailByProfileApi(long memberId, ReqAccountEmail reqAccountEmail);

    Executor<ResAvatarsInfo> loadGetAvartarsInfo();

}
