package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IProfileApiLoader {

    IExecutor<ResLeftSideMenu.User> loadUpdateMemberProfileByProfileApi(int memberId, ReqUpdateProfile reqUpdateProfile);

    IExecutor<ResCommon> loadUpdateMemberNameByProfileApi(int memberId, ReqProfileName reqProfileName);

    IExecutor<ResLeftSideMenu.User> loadUpdateMemberEmailByProfileApi(int memberId, ReqAccountEmail reqAccountEmail);

}
