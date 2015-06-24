package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IProfileApiLoader {

    IExecutor loadUpdateMemberProfileByProfileApi(int memberId, ReqUpdateProfile reqUpdateProfile);

    IExecutor loadUpdateMemberNameByProfileApi(int memberId, ReqProfileName reqProfileName);

    IExecutor loadUpdateMemberEmailByProfileApi(int memberId, ReqAccountEmail reqAccountEmail);

}
