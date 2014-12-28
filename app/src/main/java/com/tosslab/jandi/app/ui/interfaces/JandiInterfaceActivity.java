package com.tosslab.jandi.app.ui.interfaces;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.tosslab.jandi.app.ui.interfaces.actions.Action;
import com.tosslab.jandi.app.ui.interfaces.actions.ActionFactory;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..<br/>
 * It's for {tosslabjandi://xxx?yyy=zzz} Intent
 */
@EActivity
public class JandiInterfaceActivity extends Activity {

    @AfterViews
    void initView() {

        Intent intent = getIntent();
        Uri data = intent.getData();

        Action action = ActionFactory.getAction(JandiInterfaceActivity.this, data.getPath());
        action.execute(data);

    }
}
