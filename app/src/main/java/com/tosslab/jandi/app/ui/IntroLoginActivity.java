package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResAuthToken;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
@EActivity(R.layout.activity_login_final)
public class IntroLoginActivity extends Activity {
    private final Logger log = Logger.getLogger(IntroLoginActivity.class);
    @Extra
    String myId;
    @Extra
    String jsonExtraTeam;
    @ViewById(R.id.txt_login_displayed_team_name)
    TextView textViewDisplayedTeamName;
    @ViewById(R.id.txt_login_displayed_id)
    TextView textViewDisplayedId;
    @ViewById(R.id.et_login_final_password)
    EditText editTextPassword;
    @ViewById(R.id.btn_login_final)
    Button buttonLogin;
    @RestService
    JandiRestClient mJandiRestClient;

    private int mSelectedTeamId;
    private ProgressWheel mProgressWheel;
    private JandiAuthClient mJandiAuthClient;
    private JandiEntityClient mJandiEntityClient;
    private InputMethodManager imm;

    @AfterViews
    void initView() {
        setUpActionBar();
        String teamName = "";
        try {
            ResMyTeam.Team myTeam = convertJsonToPojo();
            mSelectedTeamId = myTeam.teamId;
            teamName = myTeam.name;
        } catch (IOException e) {
            ColoredToast.showError(this, "Parsing Error");
            finish();
        }

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
        // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 로그인 관련 Network Client 설정
        mJandiAuthClient = new JandiAuthClient(mJandiRestClient);

        setView(teamName);
        setActivationColorForButton();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private ResMyTeam.Team convertJsonToPojo() throws IOException {
        log.debug("JSON Extra : " + jsonExtraTeam);
        return new ObjectMapper().readValue(jsonExtraTeam, ResMyTeam.Team.class);
    }

    private void setView(String teamName) {
        // Team Name
        textViewDisplayedTeamName.setText(teamName);
        // E-mail 주소 출력
        textViewDisplayedId.setText(myId);
    }

    private void setActivationColorForButton() {
        // 텍스트에 글이 있으면 버튼 색상 변경
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0) {
                    buttonLogin.setBackgroundResource(R.drawable.btn_send_selector);
                } else {
                    buttonLogin.setBackgroundResource(R.color.jandi_inactive_button);
                }
            }
        });
    }

    /************************************************************
     * 로그인 수행
     ************************************************************/

    @Click(R.id.btn_login_final)
    void doLogin() {
        hideSoftKeyboard();
        pressLoginButton(editTextPassword.getEditableText().toString());
    }

    private void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(),0);
    }

    public void pressLoginButton(String passwd) {
        // 입력의 포멧 체크
        if (FormatConverter.isInvalidPasswd(passwd)) {
            ColoredToast.showError(this, getString(R.string.err_login_invalid_passwd));
            return;
        }

        mProgressWheel.show();
        doLoginInBackground(passwd);
    }

    @Background
    void doLoginInBackground(String passwd) {
        try {
            ResAuthToken resAuthToken = mJandiAuthClient.login(mSelectedTeamId, myId, passwd);
            JandiPreference.setMyId(this, myId);
            if (resAuthToken != null) {
                doLoginSucceed(resAuthToken);
            } else {
                doLoginFailed(R.string.err_login);
            }
        }  catch (JandiNetworkException e) {
            if (e.errCode == 1818) {
                doLoginFailed(R.string.err_login_invalid_info);
            } else {
                log.error("Login failed", e);
                doLoginFailed(R.string.err_login);
            }
        } catch (Exception e) {
            log.error("Login Fail", e);
            doLoginFailed(R.string.err_login);
        }
    }

    @UiThread
    void doLoginSucceed(ResAuthToken token) {
        registerPushTokenInBackground(token.token);
    }

    @UiThread
    void doLoginFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, getString(errMessageResId));
    }

    /************************************************************
     * for Push notification token
     ************************************************************/
    @Background
    public void registerPushTokenInBackground(String myAccessToken) {
        String oldPushToken = JandiPreference.getPushToken(this);
        String newPushToken = JandiPreference.getPushTokenToBeUpdated(this);
        log.debug("oldPushToken = " + oldPushToken);
        log.debug("newPushToken = " + newPushToken);
        try {
            mJandiEntityClient = new JandiEntityClient(mJandiRestClient, myAccessToken);

            if (newPushToken.isEmpty() == false) {
                mJandiEntityClient.registerNotificationToken(oldPushToken, newPushToken);
                log.debug("registering push token succeed, registration ID=" + newPushToken);
                sendRegistrationIdSucceed(myAccessToken, newPushToken);
            } else {
                sendRegistrationIdSucceed(myAccessToken, oldPushToken);
            }
        } catch (JandiNetworkException e) {
            log.error(e.getErrorInfo() + "Register Fail", e);
            sendRegistrationIdFailed(e.getErrorInfo());
        }
    }

    @UiThread
    public void sendRegistrationIdSucceed(String myAccessToken, String updatedToken) {
        mProgressWheel.dismiss();
        JandiPreference.setMyToken(this, myAccessToken);

        // 토큰 갱신이 성공했기 때문에 새로운 토큰을 push token 으로 저장.
        JandiPreference.setPushToken(this, updatedToken);
        JandiPreference.setPushTokenToBeUpdated(this, "");
        // 토큰 갱신이 성공했으므로 현재 버전을 저장
        JandiPreference.setPriorAppVersion(this, getThisAppVersion());
        moveToMainActivity();
    }

    private int getThisAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @UiThread
    public void sendRegistrationIdFailed(String message) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, message);
    }


    public void moveToMainActivity() {
        // MainActivity 이동
        MainTabActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        finish();
    }
}

