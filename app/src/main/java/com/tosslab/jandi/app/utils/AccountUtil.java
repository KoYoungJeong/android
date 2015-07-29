package com.tosslab.jandi.app.utils;

import android.content.Context;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by tonyjs on 15. 7. 29..
 */
public class AccountUtil {
    public static String getAccountId(Context context) {
        if (context == null) {
            return null;
        }

        ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(context).getAccountInfo();

        return accountInfo != null ? accountInfo.getId() : null;
    }

    public static int getMemberId(Context context) {
        if (context == null) {
            return -1;
        }

        ResAccountInfo.UserTeam selectedTeamInfo =
                JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

        return selectedTeamInfo != null ? selectedTeamInfo.getMemberId() : -1;
    }

}
