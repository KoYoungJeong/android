package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import java.util.List;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IInvitationApiLoader {

    Executor<ResTeamDetailInfo> loadAcceptOrDeclineInvitationByInvitationApi(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore);

    Executor<List<ResPendingTeamInfo>> loadGetPendingTeamInfoByInvitationApi();

}
