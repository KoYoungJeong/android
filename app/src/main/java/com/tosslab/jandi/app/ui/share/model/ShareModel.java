package com.tosslab.jandi.app.ui.share.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.share.ShareActivity;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
public class ShareModel {

    private final Context context;

    public ShareModel(Context context) {
        this.context = context;
    }


    public String handleSendText(Intent intent) {
        return intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    public Uri handleSendImage(Intent intent) {
        return (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    public ShareActivity.IntentType getIntentType(String action, String type) {

        if (!TextUtils.equals(action, Intent.ACTION_SEND) || TextUtils.isEmpty(type)) {
            return null;
        }

        if (type.startsWith("image/")) {
            return ShareActivity.IntentType.Image;
        } else if (TextUtils.equals(type, "text/plain")) {
            return ShareActivity.IntentType.Text;
        }

        return null;
    }

    public boolean hasTeamInfo() {

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

        return selectedTeamInfo != null;
    }

    public boolean hasEntityInfo() {

        try {
            EntityManager entityManager = EntityManager.getInstance(context);
            return entityManager != null;
        } catch (Exception e) {

            return false;
        }

    }
}
