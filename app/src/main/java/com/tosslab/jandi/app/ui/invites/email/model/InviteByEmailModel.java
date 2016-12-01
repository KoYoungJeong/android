package com.tosslab.jandi.app.ui.invites.email.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.LanguageUtil;

import java.util.Arrays;
import java.util.List;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class InviteByEmailModel {

    Lazy<TeamApi> teamApi;

    public InviteByEmailModel(Lazy<TeamApi> teamApi) {
        this.teamApi = teamApi;
    }


    public boolean isValidEmailFormat(String text) {
        return !FormatConverter.isInvalidEmailString(text);
    }

    public Observable<String> getInviteMemberObservable(final String email) {
        return Observable.<String>create(subscriber -> {
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();

                ReqInvitationMembers reqInvitationMembers =
                        new ReqInvitationMembers(teamId, Arrays.asList(email), LanguageUtil.getLanguage());

                teamApi.get().inviteToTeam(teamId, reqInvitationMembers);
                subscriber.onNext(email);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
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

    public String getCurrentTeamName() {
        return TeamInfoLoader.getInstance().getTeamName();
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

}
