package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.CreateTeamDialog;
import com.tosslab.jandi.app.dialogs.SelectTeamDialog;
import com.tosslab.jandi.app.events.RequestTeamCreationEvent;
import com.tosslab.jandi.app.events.SelectMyTeam;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 9. 24..
 */
@Fullscreen
@EActivity(R.layout.activity_login_intro)
public class IntroSelectTeamActivity extends Activity {
    private final Logger log = Logger.getLogger(IntroSelectTeamActivity.class);

    @ViewById(R.id.et_login_id)
    EditText editTextLoginId;
    @ViewById(R.id.btn_login_start)
    Button buttonLoginStart;
    @ViewById(R.id.txt_team_creation)
    TextView textViewTeamCreation;
    @RestService
    JandiRestClient jandiRestClient;

    private ProgressWheel mProgressWheel;
    private JandiAuthClient mJandiAuthClient;
    private InputMethodManager imm;
    private String mMyEmailId;

    @AfterViews
    void initView() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
        // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 로그인 관련 Network Client 설정
        mJandiAuthClient = new JandiAuthClient(jandiRestClient);

        setActivationColorForButton();
    }

    private void setActivationColorForButton() {
        textViewTeamCreation.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        // 텍스트에 글이 있으면 버튼 색상 변경
        editTextLoginId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0) {
                    buttonLoginStart.setBackgroundResource(R.drawable.btn_send_selector);
                } else {
                    buttonLoginStart.setBackgroundResource(R.color.jandi_inactive_button);
                }
            }
        });
    }

    @Click(R.id.btn_login_start)
    void getTeamList() {
        hideSoftKeyboard();
        pressButton(editTextLoginId.getEditableText().toString());
    }

    @Click(R.id.txt_team_creation)
    void createTeam() {
        showTeamTeamCreationFragment();
    }

    private void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextLoginId.getWindowToken(),0);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /************************************************************
     * Team 생성
     ************************************************************/

    private void showTeamTeamCreationFragment() {
        String email = editTextLoginId.getText().toString();
        DialogFragment newFragment = CreateTeamDialog.newInstance(email);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(RequestTeamCreationEvent event) {
        String mail = event.email;
        if (isValidEmailFormat(mail) == false) {
            return;
        }
        mProgressWheel.show();
        createTeamInBackground(mail);
    }

    @Background
    void createTeamInBackground(String myEmailId) {
        try {
            // 나의 팀 ID 획득
            ResCommon res = mJandiAuthClient.createTeam(myEmailId);
            createTeamSucceed();
        } catch (JandiNetworkException e) {
            log.error("getTeamListInBackground", e);
            createTeamFailed(R.string.err_team_creation_failed);

        } catch (Exception e) {
            log.error("getTeamListInBackground", e);
            createTeamFailed(R.string.err_network);
        }
    }

    @UiThread
    void createTeamSucceed() {
        mProgressWheel.dismiss();
        ColoredToast.showLong(this, getString(R.string.jandi_team_creation_succeed));
        finish();
    }

    @UiThread
    void createTeamFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, getString(errMessageResId));
    }

    /************************************************************
     * Team List 획득
     ************************************************************/
    public void pressButton(String myEmailId) {
        if (isValidEmailFormat(myEmailId) == false) {
            return;
        }
        mProgressWheel.show();
        getTeamListInBackground(myEmailId);
    }

    private boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        if (FormatConverter.isInvalidEmailString(email)) {
            ColoredToast.showError(this, getString(R.string.err_login_invalid_id));
            return false;
        }
        return true;
    }

    @Background
    void getTeamListInBackground(String myEmailId) {
        assert myEmailId != null : "myId cannot be null";
        try {
            // 나의 팀 ID 획득
            ResMyTeam resMyTeam = mJandiAuthClient.getMyTeamId(myEmailId);
            if (resMyTeam.teamList.size() > 0) {
                getTeamListSucceed(myEmailId, resMyTeam);
                return;
            } else {
                getTeamListFailed(R.string.err_login_unregistered_id);
            }
        } catch (JandiNetworkException e) {
            log.error("getTeamListInBackground", e);
            getTeamListFailed(R.string.err_network);

        } catch (Exception e) {
            log.error("getTeamListInBackground", e);
            getTeamListFailed(R.string.err_network);
        }
    }

    @UiThread
    void getTeamListSucceed(String myEmailId, ResMyTeam resMyTeam) {
        mProgressWheel.dismiss();
        try {
            mMyEmailId = myEmailId;
            String jsonExtraTeamList = convertPojoToJson(resMyTeam);
            showTeamSelectFragment(jsonExtraTeamList);
        } catch (IOException e) {
            ColoredToast.showError(this, "");
        }
    }

    @UiThread
    void getTeamListFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, getString(errMessageResId));
    }

    private void showTeamSelectFragment(String jsonTeamList) {
        DialogFragment newFragment = SelectTeamDialog.newInstance(jsonTeamList);
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void onEvent(SelectMyTeam myTeam) {
        try {
            String jsonExtraTeam = convertPojoToJson(myTeam.myTeam);
            moveToLoginInputPasswordActivity(mMyEmailId, jsonExtraTeam);
        } catch (IOException e) {
            ColoredToast.showError(this, "");
        }
    }

    private String convertPojoToJson(ResMyTeam.Team myTeam) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(myTeam);
    }

    private String convertPojoToJson(ResMyTeam myTeamList) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(myTeamList);
    }

    private void moveToLoginInputPasswordActivity(String myId, String jsonExtraTeam) {
        IntroLoginActivity_.intent(this)
                .myId(myId)
                .jsonExtraTeam(jsonExtraTeam)
                .start();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}
