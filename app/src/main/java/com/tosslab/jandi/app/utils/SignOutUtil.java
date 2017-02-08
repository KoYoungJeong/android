package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.Intent;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.services.keep.KeepExecutedService;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import io.intercom.android.sdk.Intercom;

public class SignOutUtil {
    public static void removeSignData() {
        Context context = JandiApplication.getContext();
        JandiPreference.signOut(context);
        BadgeUtils.clearBadge(context);
        TeamInfoLoader instance = TeamInfoLoader.getInstance();
        instance = null;
        OpenHelperManager.getHelper(context, OrmDatabaseHelper.class)
                .clearAllData();
        InitialInfoRepository.getInstance().clear();
        TokenUtil.clearTokenInfo();

        Intercom.client().reset();

        context.stopService(new Intent(context, KeepExecutedService.class));

    }

    public static void initSignData() {
        BadgeUtils.clearBadge(JandiApplication.getContext());
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .clearAllData();
        InitialInfoRepository.getInstance().clear();
        TokenUtil.clearTokenInfo();
    }

}
