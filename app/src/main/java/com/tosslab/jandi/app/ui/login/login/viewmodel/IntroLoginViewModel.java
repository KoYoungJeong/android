package com.tosslab.jandi.app.ui.login.login.viewmodel;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.ui.TeamSelectionActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginViewModel {

    @RootContext
    Activity activity;

    @ViewById(R.id.et_intro_login_email)
    EditText editTextLogin;
    @ViewById(R.id.btn_intro_action_signin_start)
    Button buttonSignInStart;


    private ViewCallback viewCallback;

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
        editTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (FormatConverter.isInvalidEmailString(editable.toString())) {
                    buttonSignInStart.setSelected(false);
                } else {
                    buttonSignInStart.setSelected(true);
                }
            }
        });
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

    public String getEmailFromEditText() {
        return editTextLogin.getText().toString();
    }

    public boolean isEmailButtonSelected() {
        return buttonSignInStart.isSelected();
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
    public void getTeamListSucceed(String myEmailId, ResMyTeam resMyTeam) {
        dissmissProgressDialog();
        setReadFlagForTutorial();
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String jsonExtraTeamList = ow.writeValueAsString(resMyTeam);
            moveToTeamSelectionActivity(myEmailId, jsonExtraTeamList);
        } catch (IOException e) {
            ColoredToast.showError(activity, "");
        }
    }

    @SupposeUiThread
    void moveToTeamSelectionActivity(String myEmailId, String jsonExtraTeamList) {
        TeamSelectionActivity_.intent(activity)
                .recievedEmail(myEmailId)
                .jsonExtraTeamList(jsonExtraTeamList)
                .start();
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }


    @UiThread
    public void getTeamListFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(activity, activity.getString(errMessageResId));
    }

    @SupposeUiThread
    void setReadFlagForTutorial() {
        JandiPreference.setFlagForTutorial(activity, true);
    }

    /**
     * Create Team
     */
    @Click(R.id.btn_getting_started)
    void showTeamTeamCreationFragment() {
        if (viewCallback != null) {
            viewCallback.onTeamCreate();
        }
    }

    /**
     * Get Team List
     */
    @Click(R.id.btn_intro_action_signin_start)
    void startLogin() {
        if (viewCallback != null) {
            viewCallback.onLogin();
        }
    }

    public void hideKeypad() {
        imm.hideSoftInputFromWindow(editTextLogin.getWindowToken(), 0);
    }

    public void setViewCallback(ViewCallback viewCallback) {
        this.viewCallback = viewCallback;
    }

    public interface ViewCallback {
        void onTeamCreate();

        void onLogin();
    }

}
