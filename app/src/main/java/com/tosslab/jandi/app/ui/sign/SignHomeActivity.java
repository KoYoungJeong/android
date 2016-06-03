package com.tosslab.jandi.app.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.sign.signin.SignInActivity;
import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tonyjs on 16. 6. 3..
 */
public class SignHomeActivity extends BaseAppCompatActivity {

    @Bind(R.id.iv_jandi_icon)
    View vIcon;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_home);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);
        ButterKnife.bind(this);

        ViewCompat.setTransitionName(vIcon, "icon_anim");
    }

    @OnClick(R.id.btn_sign_up)
    public void onClickSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.StartPage, AnalyticsValue.Action.SignUp);
    }

    @OnClick(R.id.btn_sign_in)
    public void onClickSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.StartPage, AnalyticsValue.Action.SignIn);
    }
}
