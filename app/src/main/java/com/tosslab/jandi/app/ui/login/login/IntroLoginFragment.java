package com.tosslab.jandi.app.ui.login.login;

import android.app.DialogFragment;
import android.app.Fragment;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.TeamCreationDialogFragment;
import com.tosslab.jandi.app.events.RequestTeamCreationEvent;
import com.tosslab.jandi.app.network.models.ResMyTeam;
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
            public void onGetTeamListSuccess(String myEmailId, ResMyTeam resMyTeam) {
                introLoginViewModel.getTeamListSucceed(myEmailId, resMyTeam);
            }

            @Override
            public void onGetTeamListFail(int errorStringResId) {
                introLoginViewModel.getTeamListFailed(errorStringResId);
            }
        });

        introLoginViewModel.setViewCallback(new IntroLoginViewModel.ViewCallback() {
            @Override
            public void onTeamCreate() {
                showTeamTeamCreationFragment();
            }

            @Override
            public void onLogin() {
                startLogin();
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


    private void showTeamTeamCreationFragment() {
        // belong to Fragment? LoginViewModel?
        String email = introLoginViewModel.getEmailFromEditText();
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

    private void startLogin() {
        // belong to Fragment? LoginViewModel?
        if (introLoginViewModel.isEmailButtonSelected()) {
            introLoginViewModel.hideKeypad();

            introLoginViewModel.showProgressDialog();
            introLoginModel.getTeamListInBackground(introLoginViewModel.getEmailFromEditText());
        }
    }

}
