package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResStarred;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ITeamApiAuth {

    ResTeamDetailInfo createNewTeamByTeamApi(ReqCreateNewTeam req) throws RetrofitError;

    ResLeftSideMenu.User getMemberProfileByTeamApi(int teamId, int memberId) throws RetrofitError;

    List<ResInvitationMembers> inviteToTeamByTeamApi(int teamId, ReqInvitationMembers invitationMembers) throws RetrofitError;

    ResTeamDetailInfo.InviteTeam getTeamInfoByTeamApi(int teamId) throws RetrofitError;

    ResAnnouncement getAnnouncement(int teamId, int topicId) throws RetrofitError;

    ResCommon createAnnouncement(int teamId, int topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitError;

    ResCommon updateAnnouncementStatus(int teamId, int memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitError;

    ResCommon deleteAnnouncement(int teamId, int topicId) throws RetrofitError;

    ResMessages.OriginalMessage getMessage(int teamId, int messageId) throws RetrofitError;

    ResStarMentioned getMentionedMessagesByTeamApi(int teamId, int page, int perPage) throws RetrofitError;

    ResStarred registStarredMessageByTeamApi(int teamId, int messageId) throws RetrofitError;

    ResCommon unregistStarredMessageByTeamApi(int teamId, int messageId) throws RetrofitError;

    ResStarMentioned getStarredMessagesByTeamApi(int teamId, String type,
                                             int page, int perPage);

}
