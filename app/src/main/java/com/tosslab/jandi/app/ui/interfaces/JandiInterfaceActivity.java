package com.tosslab.jandi.app.ui.interfaces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.interfaces.actions.Action;
import com.tosslab.jandi.app.ui.interfaces.actions.ActionFactory;

/**
 * It's for {tosslabjandi://xxx?yyy=zzz} Intent
 */
public class JandiInterfaceActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
        initView();
    }

    void initView() {
        // renew parse push infomation before getting access
        boolean used = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        Intent intent = getIntent();
        Uri data;
        if (!used) {
            data = intent.getData();
        } else {
            data = Uri.parse("http://");
        }

        Action action = ActionFactory.getAction(JandiInterfaceActivity.this, data);
        action.execute(data);

    }
}
