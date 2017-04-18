package com.tosslab.jandi.app.ui.team.select.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.marker.MarkerApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResOnlineStatus;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.marker.Marker;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tee on 2016. 9. 27..
 */

public class TeamSelectListModel {

    private Lazy<InvitationApi> invitationApi;
    private Lazy<AccountApi> accountApi;
    private Lazy<StartApi> startApi;
    private Lazy<TeamApi> teamApi;
    private Lazy<MarkerApi> markerApi;

    @Inject
    public TeamSelectListModel(Lazy<InvitationApi> invitationApi,
                               Lazy<AccountApi> accountApi,
                               Lazy<StartApi> startApi,
                               Lazy<TeamApi> teamApi,
                               Lazy<MarkerApi> markerApi) {
        this.invitationApi = invitationApi;
        this.accountApi = accountApi;
        this.startApi = startApi;
        this.teamApi = teamApi;
        this.markerApi = markerApi;
    }

    public void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
            AccountUtil.removeDuplicatedTeams(resAccountInfo);
            AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    public ResTeamDetailInfo acceptOrDeclineInvite(String invitationId, String type) throws RetrofitException {

        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();

        if (accountInfo == null) {
            return null;
        }

        ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore = new ReqInvitationAcceptOrIgnore(type);
        return invitationApi.get().acceptOrDeclineInvitation(invitationId, reqInvitationAcceptOrIgnore);
    }

    public List<Team> getTeamInfos() throws RetrofitException {
        List<Team> teams = new ArrayList<Team>();

        List<ResPendingTeamInfo> pendingTeamInfo = invitationApi.get().getPedingTeamInfo();

        for (int idx = pendingTeamInfo.size() - 1; idx >= 0; idx--) {
            if (!TextUtils.equals(pendingTeamInfo.get(idx).getStatus(), "pending")) {
                pendingTeamInfo.remove(idx);
            }
        }

        teams.addAll(convertPedingTeamList(pendingTeamInfo));

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();
        teams.addAll(convertJoinedTeamList(userTeams));

        teams.add(Team.createEmptyTeam());

        return teams;
    }

    private List<Team> convertJoinedTeamList(List<ResAccountInfo.UserTeam> memberships) {
        List<Team> teams = new ArrayList<Team>();

        if (memberships == null) {
            return teams;
        }

        for (ResAccountInfo.UserTeam membership : memberships) {
            teams.add(Team.createTeam(membership));
        }

        return teams;
    }

    private List<Team> convertPedingTeamList(List<ResPendingTeamInfo> pedingTeamInfos) {
        List<Team> teams = new ArrayList<Team>();

        if (pedingTeamInfos == null) {
            return teams;
        }

        for (ResPendingTeamInfo pedingTeamInfo : pedingTeamInfos) {
            teams.add(Team.createTeam(pedingTeamInfo));
        }

        return teams;
    }

    public void updateSelectTeam(long teamId) {
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public String getEntityInfo(long teamId) throws RetrofitException {
        return startApi.get().getRawInitializeInfo(teamId);
    }

    public void updateEntityInfo(RawInitialInfo entityInfo) {
        InitialInfoRepository.getInstance().upsertRawInitialInfo(entityInfo);
    }

    public void updateTeamInfo(long teamId) throws RetrofitException {
        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public String getMyEmail() {
        List<ResAccountInfo.UserEmail> emails = AccountRepository.getRepository().getAccountEmails();
        if (emails == null || emails.isEmpty()) {
            return "";
        }
        int length = emails.size();
        String primaryEmail = emails.get(0).getEmail();
        for (int i = 0; i < length; i++) {
            if (emails.get(i).isPrimary()) {
                primaryEmail = emails.get(i).getEmail();
            }
        }
        return primaryEmail;
    }

    public void refreshRankIfNeed(long teamId) {
        if (!RankRepository.getInstance().hasRanks(teamId)) {
            try {
                Ranks ranks = teamApi.get().getRanks(teamId);
                RankRepository.getInstance().addRanks(ranks.getRanks());
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateOnlineStatus(long teamId) {
        try {
            ResOnlineStatus resOnlineStatus = teamApi.get().getOnlineStatus(teamId);
            TeamInfoLoader.getInstance().setOnlineStatus(resOnlineStatus.getRecords());
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public void refreshMyMarker(long teamId, long myId) {
        try {
            List<Marker> markers = markerApi.get().getMarkersFromMemberId(teamId, myId);
            for (Marker marker : markers) {
                long roomId = marker.getRoomId();
                long lastLinkId = marker.getReadLinkId();
                if (TeamInfoLoader.getInstance().isTopic(roomId)) {
                    TopicRepository.getInstance(teamId).updateReadLinkId(roomId, lastLinkId);
                    TopicRepository.getInstance(teamId).updateUnreadCount(roomId, 0);
                } else if (TeamInfoLoader.getInstance().isChat(roomId)) {
                    ChatRepository.getInstance(teamId).updateReadLinkId(roomId, lastLinkId);
                    ChatRepository.getInstance(teamId).updateUnreadCount(roomId, 0);
                }
            }

            Observable.concat(
                    Observable.from(TopicRepository.getInstance(teamId).getJoinedTopics())
                            .map(Topic::getUnreadCount),
                    Observable.from(ChatRepository.getInstance(teamId).getOpenedChats())
                            .map(Chat::getUnreadCount))
                    .filter(count -> count > 0)
                    .defaultIfEmpty(0)
                    .reduce((integer, integer2) -> integer + integer2)
                    .subscribe(count -> {
                        AccountRepository.getRepository().updateUnread(teamId, count);
                    }, Throwable::printStackTrace);

        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

}