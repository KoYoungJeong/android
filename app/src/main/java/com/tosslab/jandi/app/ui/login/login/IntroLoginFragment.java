package com.tosslab.jandi.app.ui.login.login;

import android.app.DialogFragment;
import android.app.Fragment;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.TeamCreationDialogFragment;
import com.tosslab.jandi.app.events.RequestTeamCreationEvent;
import com.tosslab.jandi.app.ui.login.login.model.IntroLoginModel;
import com.tosslab.jandi.app.ui.login.login.viewmodel.IntroLoginViewModel;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 13..
 */
@EFragment(R.layout.fragment_intro_input_id)
public class IntroLoginFragment extends Fragment {

    @Bean
    public IntroLoginModel introLoginModel;

    @Bean
    public IntroLoginViewModel introLoginViewModel;

    @AfterViews
    void initView() {
        introLoginModel.setCallback(new IntroLoginModel.Callback() {
            @Override
            public void onCreateTeamSuccess() {
                introLoginViewModel.createTeamSucceed();
            }

            @Override
            public void onCreateTeamFail(int stringResId) {
                introLoginViewModel.createTeamFailed(stringResId);
            }

            @Override
            public void onLoginSuccess(String myEmailId) {
                introLoginViewModel.loginSuccess(myEmailId);
            }

            @Override
            public void onLoginFail(int errorStringResId) {
                introLoginViewModel.loginFail(errorStringResId);
            }
        });

        introLoginViewModel.setViewCallback(new IntroLoginViewModel.ViewCallback() {
            @Override
            public void onTeamCreate(String email) {
                showTeamTeamCreationFragment(email);
            }

            @Override
            public void onLogin(String email, String password) {
                startLogin(email, password);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    private void showTeamTeamCreationFragment(String email) {
        // belong to Fragment? LoginViewModel?
        DialogFragment newFragment = TeamCreationDialogFragment.newInstance(email);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(RequestTeamCreationEvent event) {
        String mail = event.email;
        if (introLoginModel.isValidEmailFormat(mail)) {
            ColoredToast.showError(getActivity(), getString(R.string.err_login_invalid_id));
            return;
        }
        introLoginViewModel.showProgressDialog();
        introLoginModel.createTeamInBackground(mail);
    }

    private void startLogin(String email, String password) {
        // belong to Fragment? LoginViewModel?
        introLoginViewModel.hideKeypad();

        introLoginViewModel.showProgressDialog();
        introLoginModel.startLogin(email, password);
    }

}
