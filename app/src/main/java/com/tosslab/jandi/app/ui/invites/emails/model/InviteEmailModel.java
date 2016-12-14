package com.tosslab.jandi.app.ui.invites.emails.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.invites.emails.vo.InviteEmailVO;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.LanguageUtil;

import java.util.ArrayList;
import java.util.List;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tee on 2016. 12. 12..
 */

public class InviteEmailModel {

    Lazy<TeamApi> teamApi;

    public InviteEmailModel(Lazy<TeamApi> teamApi) {
        this.teamApi = teamApi;
    }

    public boolean isValidEmailFormat(String text) {
        return !FormatConverter.isInvalidEmailString(text);
    }

    public boolean isInactivedUser(String email) {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isInactive)
                .filter(user -> TextUtils.equals(user.getEmail(), email))
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

    public boolean isNotEnableUser(String email) {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(user1 -> !user1.isEnabled())
                .filter(user -> TextUtils.equals(user.getEmail(), email))
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

    public boolean isInvitedEmail(String emailText) {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository()
                .getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (TextUtils.equals(emailText, userEmail.getId())) {
                return true;
            }
        }
        List<User> users = TeamInfoLoader.getInstance().getUserList();

        Boolean isContain = Observable.from(users)
                .filter(user -> user.getId() != TeamInfoLoader.getInstance().getMyId())
                .filter(entity -> TextUtils.equals(entity.getEmail(), emailText)
                        && entity.isEnabled())
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();

        return isContain;
    }

    public List<ResInvitationMembers> sendInviteEmailForAssociate(List<InviteEmailVO> emailVOs, long topicId) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();

            ArrayList<String> emails = new ArrayList<>();

            Observable.from(emailVOs)
                    .subscribe(emailVO -> {
                        if (emailVO.getStatus() == InviteEmailVO.Status.AVAILABLE) {
                            emails.add(emailVO.getEmail());
                        }
                    });

            ReqInvitationMembers reqInvitationMembers =
                    new ReqInvitationMembers(teamId, emails, LanguageUtil.getLanguage()
                            , 0, topicId);

            return teamApi.get().inviteToTeam(teamId, reqInvitationMembers);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ResInvitationMembers> sendInviteEmailForMember(List<InviteEmailVO> emailVOs) {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();

            ArrayList<String> emails = new ArrayList<>();

            Observable.from(emailVOs)
                    .subscribe(emailVO -> {
                        if (emailVO.getStatus() == InviteEmailVO.Status.AVAILABLE) {
                            emails.add(emailVO.getEmail());
                        }
                    });

            ReqInvitationMembers reqInvitationMembers =
                    new ReqInvitationMembers(teamId, emails, LanguageUtil.getLanguage()
                            , 1, -1);

            return teamApi.get().inviteToTeam(teamId, reqInvitationMembers);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
        return null;
    }

}