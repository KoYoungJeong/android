package com.tosslab.jandi.app.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.sign.signin.SignInActivity;
import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignHomeActivity extends BaseAppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_home);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);
        ButterKnife.bind(this);
        SignOutUtil.initSignData();
    }

    @OnClick(R.id.btn_sign_up)
    public void onClickSignUp() {
        if (!NetworkCheckUtil.isConnected()) {
            ColoredToast.showError(R.string.jandi_msg_network_offline_warn);
            return;
        }

        SignUpActivity.startActivity(SignHomeActivity.this, null);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.StartPage, AnalyticsValue.Action.SignUp);
        AnalyticsUtil.sendConversion("Android_Register", "957512006", "l9F-CIeql2MQxvLJyAM");

    }

    @OnClick(R.id.vg_sign_in)
    public void onClickSignIn() {
        if (!NetworkCheckUtil.isConnected()) {
            ColoredToast.showError(R.string.jandi_msg_network_offline_warn);
            return;
        }

        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.StartPage, AnalyticsValue.Action.SignIn);
    }
}
