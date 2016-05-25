package com.tosslab.jandi.app.ui.intro.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.signin.MainSignInActivity;
import com.tosslab.jandi.app.ui.intro.signup.MainSignUpActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 16. 5. 25..
 */

public class MainHomeActivity extends BaseAppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jandi_main_home);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tv_sign_up_button)
    public void onClickSignUp() {
        Intent intent = new Intent(this, MainSignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.tv_sign_in_button)
    public void onClickSignIn() {
        Intent intent = new Intent(this, MainSignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}