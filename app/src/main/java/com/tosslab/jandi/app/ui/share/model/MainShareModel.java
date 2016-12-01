package com.tosslab.jandi.app.ui.share.model;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.share.MainShareActivity;

import java.util.List;

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
        if (TextUtils.equals(action, Intent.ACTION_SEND) && !TextUtils.isEmpty(type)) {
            if (type.toLowerCase().matches("text/.*")
                    || type.toLowerCase().matches("message/.*")) {
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
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            return InitialInfoRepository.getInstance().hasInitialInfo(selectedTeamId);
        } catch (Exception e) {
            return false;
        }
    }

}
