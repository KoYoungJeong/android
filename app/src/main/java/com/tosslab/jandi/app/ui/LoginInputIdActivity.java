package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
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

/**
 * Created by justinygchoi on 2014. 9. 24..
 */
@Fullscreen
@EActivity(R.layout.activity_login_intro)
public class LoginInputIdActivity extends Activity {
    private final Logger log = Logger.getLogger(LoginInputIdActivity.class);

    @ViewById(R.id.et_login_id)
    EditText editTextLoginId;
    @ViewById(R.id.btn_login_start)
    Button buttonLoginStart;
    @RestService
    JandiRestClient jandiRestClient;

    private ProgressWheel mProgressWheel;
    private JandiAuthClient mJandiAuthClient;
    private InputMethodManager imm;

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

    private void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextLoginId.getWindowToken(),0);
    }

    /************************************************************
     * Team List 획득
     ************************************************************/
    public void pressButton(String myId) {
        // ID 입력의 포멧 체크
        if (FormatConverter.isInvalidEmailString(myId)) {
            ColoredToast.showError(this, getString(R.string.err_login_invalid_id));
            return;
        }
        mProgressWheel.show();
        getTeamListInBackground(myId);
    }

    @Background
    void getTeamListInBackground(String myId) {
        assert myId != null : "myId cannot be null";
        try {
            // 나의 팀 ID 획득
            ResMyTeam resMyTeam = mJandiAuthClient.getMyTeamId(myId);
            if (resMyTeam.teamList.size() > 0) {
                getTeamListSucceed(myId, resMyTeam);
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
    void getTeamListSucceed(String myId, ResMyTeam resMyTeam) {
        mProgressWheel.dismiss();
        try {
            if (resMyTeam.teamList.size() == 1) {
                // 팀이 한개 밖에 없기에 바로 패스워드 입력으로 간다.
                log.debug("Move to password");
                String jsonExtraTeam = convertPojoToJson(resMyTeam.teamList.get(0));
                moveToLoginInputPasswordActivity(myId, jsonExtraTeam);
            } else {
                // 팀이 두개 이상이므로 팀 선택 activity로 이동한다.
                log.debug("Move to team selection");
                moveToLoginSelectTeamActivity();
            }
        } catch (IOException e) {
            ColoredToast.showError(this, "");
        }

    }

    @UiThread
    void getTeamListFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, getString(errMessageResId));
    }

    private void moveToLoginInputPasswordActivity(String myId, String jsonExtraTeam) {
        LoginInputPasswordActivity_.intent(this)
                .myId(myId)
                .jsonExtraTeam(jsonExtraTeam)
                .start();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void moveToLoginSelectTeamActivity() {
        finish();
    }

    private String convertPojoToJson(ResMyTeam.Team myTeam) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(myTeam);
    }
}
