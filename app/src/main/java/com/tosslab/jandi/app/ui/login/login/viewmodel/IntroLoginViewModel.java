package com.tosslab.jandi.app.ui.login.login.viewmodel;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
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

    @ViewById(R.id.txt_intro_login_forgot_password)
    TextView forgotPasswordView;

    @ViewById(R.id.layout_tutorial_top)
    View topLayout;

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

    @AfterViews
    void initView() {
        int rootHeight = ((View) topLayout.getParent().getParent().getParent()).getMeasuredHeight();

        ViewGroup.LayoutParams topLayoutParams = topLayout.getLayoutParams();
        topLayoutParams.height = rootHeight / 2;
        topLayout.setLayoutParams(topLayoutParams);
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

        moveToTeamSelectionActivity(myEmailId);
    }

    @SupposeUiThread
    void moveToTeamSelectionActivity(String myEmailId) {
        AccountHomeActivity_.intent(activity)
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

    public void setEmailText(String email) {
        editTextEmail.setText(email);
        editTextEmail.setSelection(email.length());
    }

    public String getPasswordText() {
        return editTextPassword.getText().toString();
    }

    public void hideKeypad() {
        imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
    }

    @UiThread
    public void showFailPasswordResetToast() {
        ColoredToast.show(activity, activity.getString(R.string.jandi_fail_send_password_reset_email));
    }

    @UiThread
    public void showSuccessPasswordResetToast() {
        ColoredToast.show(activity, activity.getString(R.string.jandi_sent_password_reset_mail));
    }

    public void setSignInButtonEnable(boolean validEmailPassword) {
        buttonSignInStart.setEnabled(validEmailPassword);
    }
}
