package com.tosslab.jandi.app.ui.invites.email.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.LanguageUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
@EBean
public class InviteEmailModel {

    @RootContext
    Context context;

    public boolean isValidEmailFormat(String text) {
        return !FormatConverter.isInvalidEmailString(text);
    }

    public List<ResInvitationMembers> inviteMembers(List<String> invites) throws RetrofitError {

        int teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();

        return RequestApiManager.getInstance().inviteToTeamByTeamApi(teamId, new ReqInvitationMembers(teamId, invites, LanguageUtil.getLanguage(context.getApplicationContext())));

    }

    public boolean isInvitedEmail(String emailText) {
        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (TextUtils.equals(emailText, userEmail.getId())) {
                return true;
            }
        }
        List<FormattedEntity> users = EntityManager.getInstance(context).getFormattedUsersWithoutMe();

        Boolean isContain = Observable.from(users)
                .filter(entity -> TextUtils.equals(entity.getUserEmail(), emailText))
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();

        return isContain;
    }

}
