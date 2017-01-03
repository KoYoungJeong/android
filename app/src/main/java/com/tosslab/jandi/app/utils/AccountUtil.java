package com.tosslab.jandi.app.utils;

import android.content.Context;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import rx.Observable;

/**
 * Created by tonyjs on 15. 7. 29..
 */
public class AccountUtil {
    public static String getAccountId(Context context) {
        if (context == null) {
            return null;
        }

        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();

        return accountInfo != null ? accountInfo.getId() : null;
    }

    public static String getAccountUUid(Context context) {
        if (context == null) {
            return null;
        }

        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();

        return accountInfo != null ? accountInfo.getUuid() : null;
    }

    public static long getMemberId(Context context) {
        if (context == null) {
            return -1;
        }

        ResAccountInfo.UserTeam selectedTeamInfo =
                AccountRepository.getRepository().getSelectedTeamInfo();

        return selectedTeamInfo != null ? selectedTeamInfo.getMemberId() : -1;
    }

    public static void removeDuplicatedTeams(ResAccountInfo resAccountInfo) {
        if (resAccountInfo == null
                || resAccountInfo.getMemberships() == null
                || resAccountInfo.getMemberships().isEmpty()) {
            return;
        }

        Observable.from(resAccountInfo.getMemberships())
                .distinct(ResAccountInfo.UserTeam::getTeamId)
                .toList()
                .subscribe(resAccountInfo::setMemberships);

    }

}
