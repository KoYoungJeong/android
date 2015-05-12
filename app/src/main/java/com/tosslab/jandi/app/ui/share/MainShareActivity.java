package com.tosslab.jandi.app.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.share.model.MainShareModel;
import com.tosslab.jandi.app.ui.share.type.image.ImageShareDialogFragment_;
import com.tosslab.jandi.app.ui.share.type.text.TextShareDialogFragment_;
import com.tosslab.jandi.app.utils.ColoredToast;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
public class MainShareActivity extends AppCompatActivity {

    private MainShareModel mainShareModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainShareModel = new MainShareModel(MainShareActivity.this);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        IntentType intentType = mainShareModel.getIntentType(action, type);

        if (intentType == null) {
            // Check Shared Info Type
            startIntro();
            return;
        }

        if (!mainShareModel.hasTeamInfo() || !mainShareModel.hasEntityInfo()) {
            // Check Login Info
            ColoredToast.show(MainShareActivity.this, getString(R.string.err_profile_get_info));
            startIntro();
            return;
        }

        DialogFragment fragment;
        switch (intentType) {
            case Image:
                fragment = ImageShareDialogFragment_
                        .builder()
                        .uriString(mainShareModel.handleSendImage(intent).toString())
                        .build();
                break;
            case Text:
                fragment = TextShareDialogFragment_
                        .builder()
                        .subject(mainShareModel.handleSendSubject(intent))
                        .text(mainShareModel.handleSendText(intent))
                        .build();
                break;
            default:
                fragment = null;
                break;
        }

        if (fragment != null) {
            fragment.show(getSupportFragmentManager(), "dialog");
        } else {
            startIntro();
        }
    }

    private void startIntro() {
        IntroActivity_.intent(MainShareActivity.this)
                .start();
    }

    public enum IntentType {
        Image, Text
    }
}
