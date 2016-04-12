package com.tosslab.jandi.app.ui.share.model;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.share.MainShareActivity;

import org.androidannotations.annotations.EBean;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EBean
public class MainShareModel {

    public String getShareSubject(Intent intent) {
        return intent.getStringExtra(Intent.EXTRA_SUBJECT);
    }

    public CharSequence getShareText(Intent intent) {
        return intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
    }

    public Uri getShareFile(Intent intent) {
        return (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    public List<Uri> getShareFiles(Intent intent) {
        return intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    }

    public MainShareActivity.IntentType getIntentType(String action, String type) {
        Log.e("tony", "action = " + action);
        Log.e("tony", "type = " + type);
        if (TextUtils.equals(action, Intent.ACTION_SEND) && !TextUtils.isEmpty(type)) {
            if (type.toLowerCase().matches("text/.*")
                    || type.toLowerCase().matches("message/.*")) {
                Log.e("tony", "type.toLowerCase().matches(\"text/(.*)\")");
                return MainShareActivity.IntentType.Text;
            } else {
                return MainShareActivity.IntentType.File;
            }
        } else if (TextUtils.equals(action, Intent.ACTION_SEND_MULTIPLE)) {
            return MainShareActivity.IntentType.Multiple;
        } else {
            return null;
        }

    }

    public boolean hasTeamInfo() {

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        return selectedTeamInfo != null;
    }

    public boolean hasEntityInfo() {

        try {
            EntityManager entityManager = EntityManager.getInstance();
            return entityManager != null;
        } catch (Exception e) {

            return false;
        }
    }

}
