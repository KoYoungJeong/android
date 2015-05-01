package com.tosslab.jandi.app.ui.interfaces;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.ui.interfaces.actions.Action;
import com.tosslab.jandi.app.ui.interfaces.actions.ActionFactory;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..<br/>
 * It's for {tosslabjandi://xxx?yyy=zzz} Intent
 */
@EActivity
public class JandiInterfaceActivity extends AppCompatActivity {

    @AfterInject
    void initView() {

        Intent intent = getIntent();
        Uri data = intent.getData();

        Action action = ActionFactory.getAction(JandiInterfaceActivity.this, data);
        action.execute(data);

    }
}
