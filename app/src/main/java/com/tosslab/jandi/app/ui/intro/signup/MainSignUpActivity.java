package com.tosslab.jandi.app.ui.intro.signup;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.signup.presenter.MainSignUpPresenter;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by tee on 16. 5. 25..
 */

public class MainSignUpActivity extends BaseAppCompatActivity {

    @Inject
    MainSignUpPresenter mainSignUpPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jandi_sign_up);
        ButterKnife.bind(this);
    }
}