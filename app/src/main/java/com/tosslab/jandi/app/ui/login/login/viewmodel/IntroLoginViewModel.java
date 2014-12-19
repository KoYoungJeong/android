package com.tosslab.jandi.app.ui.login.login.viewmodel;

import android.app.Activity;
import android.text.Editable;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.select.TeamSelectionActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginViewModel {

    @RootContext
    Activity activity;

    @ViewById(R.id.et_intro_login_email)
    EditText editTextEmail;

    @ViewById(R.id.et_intro_login_password)
    EditText editTextPassword;

    @ViewById(R.id.btn_intro_action_signin_start)
    Button buttonSignInStart;
    /**
     * 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
     */
    @SystemService
    InputMethodManager imm;
    private ProgressWheel mProgressWheel;


    @AfterInject
    void initObject() {
        mProgressWheel = new ProgressWheel(activity);
        mProgressWheel.init();
    }

    @AfterTextChange(R.id.et_intro_login_email)
    void checkValidEmail(Editable editable) {
        if (FormatConverter.isInvalidEmailString(editable.toString())) {
            buttonSignInStart.setEnabled(false);
        } else {
            buttonSignInStart.setEnabled(true);
        }
    }

    public void showProgressDialog() {

        dissmissProgressDialog();
        mProgressWheel.show();

    }

    public void dissmissProgressDialog() {

        if (mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

    @UiThread
    public void createTeamSucceed() {
        dissmissProgressDialog();
        ColoredToast.showLong(activity, activity.getString(R.string.jandi_team_creation_succeed));
        setReadFlagForTutorial();
        activity.finish();
    }

    @UiThread
    public void createTeamFailed(int errMessageResId) {
        dissmissProgressDialog();
        ColoredToast.showError(activity, activity.getString(errMessageResId));
    }

    @UiThread
    public void loginSuccess(String myEmailId) {
        dissmissProgressDialog();
        setReadFlagForTutorial();

        moveToTeamSelectionActivity(myEmailId);
    }

    @SupposeUiThread
    void moveToTeamSelectionActivity(String myEmailId) {
        TeamSelectionActivity_.intent(activity)
                .start();
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        activity.finish();
    }


    @UiThread
    public void loginFail(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(activity, activity.getString(errMessageResId));
    }

    public String getEmailText() {
        return editTextEmail.getText().toString();
    }

    public String getPasswordText() {
        return editTextPassword.getText().toString();
    }

    @SupposeUiThread
    void setReadFlagForTutorial() {
        JandiPreference.setFlagForTutorial(activity, true);
    }

    public void hideKeypad() {
        imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
    }


}
