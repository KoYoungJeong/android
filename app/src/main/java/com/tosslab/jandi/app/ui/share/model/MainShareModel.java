package com.tosslab.jandi.app.ui.share.model;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

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

    public String handleSendSubject(Intent intent) {
        return intent.getStringExtra(Intent.EXTRA_SUBJECT);
    }

    public String handleSendText(Intent intent) {
        return intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    public Uri handleSendImage(Intent intent) {
        return (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    public List<Uri> handleSendImages(Intent intent) {
        return intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    }

    public MainShareActivity.IntentType getIntentType(String action, String type) {

        if (TextUtils.equals(action, Intent.ACTION_SEND) && !TextUtils.isEmpty(type)) {
            if (type.startsWith("image/")) {
                return MainShareActivity.IntentType.Image;
            } else if (TextUtils.equals(type, "text/plain")) {
                return MainShareActivity.IntentType.Text;
            } else {
                return MainShareActivity.IntentType.Etc;
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
