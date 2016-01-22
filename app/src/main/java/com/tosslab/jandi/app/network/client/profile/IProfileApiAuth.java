package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IProfileApiAuth {

    ResLeftSideMenu.User updateMemberProfileByProfileApi(long memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitError;

    ResCommon updateMemberNameByProfileApi(long memberId, ReqProfileName reqProfileName) throws RetrofitError;

    ResLeftSideMenu.User updateMemberEmailByProfileApi(long memberId, ReqAccountEmail reqAccountEmail) throws RetrofitError;

    ResAvatarsInfo getAvartarsInfo() throws RetrofitError;

}
