package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IInvitationApiAuth {

    ResTeamDetailInfo acceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) throws RetrofitError;

    List<ResPendingTeamInfo> getPendingTeamInfoByInvitationApi() throws RetrofitError;

}
