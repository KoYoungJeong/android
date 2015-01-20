package com.tosslab.jandi.app.ui.invites.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
@EBean
public class InviteModel {

    @RootContext
    Context context;

    public boolean isValidEmailFormat(String text) {
        return !FormatConverter.isInvalidEmailString(text);
    }

    public List<ResInvitationMembers> inviteMembers(List<String> invites) throws JandiNetworkException {

        int teamId = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();

        return RequestManager.newInstance(context, new InviteRequest(context, teamId, invites)).request();
    }

    public boolean isNotMyEmail(String emailText) {
        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (TextUtils.equals(emailText, userEmail.getId())) {
                return false;
            }
        }

        return true;
    }
}
