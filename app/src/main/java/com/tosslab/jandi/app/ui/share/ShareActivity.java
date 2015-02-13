package com.tosslab.jandi.app.ui.share;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.share.type.image.ImageShareFragment_;
import com.tosslab.jandi.app.ui.share.type.text.TextShareFragment_;
import com.tosslab.jandi.app.utils.ColoredToast;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
public class ShareActivity extends Activity {

    private ShareModel shareModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shareModel = new ShareModel(ShareActivity.this);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        IntentType intentType = shareModel.getIntentType(action, type);

        if (intentType == null) {
            // Check Shared Info Type
            ColoredToast.show(ShareActivity.this, "이미지와 텍스트를 공유해주세요.");
            startIntro();
            return;
        }

        if (!shareModel.hasTeamInfo() || !shareModel.hasEntityInfo()) {
            // Check Login Info
            ColoredToast.show(ShareActivity.this, getString(R.string.err_profile_get_info));
            startIntro();
            return;
        }

        Fragment fragment;
        switch (intentType) {
            case Image:
                fragment = ImageShareFragment_
                        .builder()
                        .uriString(shareModel.handleSendImage(intent).toString())
                        .build();
                break;
            case Text:
                fragment = TextShareFragment_
                        .builder()
                        .text(shareModel.handleSendText(intent))
                        .build();
                break;
            default:
                fragment = null;
                break;
        }

        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        } else {
            startIntro();
        }
    }

    private void startIntro() {
        IntroActivity_.intent(ShareActivity.this)
                .start();
    }

    public enum IntentType {
        Image, Text
    }
}
