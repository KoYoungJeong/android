package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IInvitationApiLoader {

    IExecutor loadAcceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore);

    IExecutor loadGetPendingTeamInfoByInvitationApi();

}
