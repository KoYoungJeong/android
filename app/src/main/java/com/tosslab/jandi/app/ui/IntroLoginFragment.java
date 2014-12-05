package com.tosslab.jandi.app.ui;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.TeamCreationDialogFragment;
import com.tosslab.jandi.app.events.RequestTeamCreationEvent;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 11. 13..
 */
@EFragment(R.layout.fragment_intro_input_id)
public class IntroLoginFragment extends Fragment {
    private final Logger log = Logger.getLogger(IntroLoginFragment.class);

    @ViewById(R.id.et_intro_login_email)
    EditText editTextLoginId;
    @ViewById(R.id.btn_intro_action_signin_start)
    Button buttonSignInStart;
    @RestService
    JandiRestClient jandiRestClient;

    private ProgressWheel mProgressWheel;
    private JandiAuthClient mJandiAuthClient;
    private InputMethodManager imm;

    @AfterViews
    void initView() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();
        // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        // 로그인 관련 Network Client 설정
        mJandiAuthClient = new JandiAuthClient(jandiRestClient);
        BadgeUtils.clearBadge(getActivity());
        setActivationColorForButton();
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
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
                if (FormatConverter.isInvalidEmailString(editable.toString())) {
                    buttonSignInStart.setSelected(false);
                } else {
                    buttonSignInStart.setSelected(true);
                }
            }
        });
    }

    @SupposeUiThread
    boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        if (FormatConverter.isInvalidEmailString(email)) {
            ColoredToast.showError(getActivity(), getString(R.string.err_login_invalid_id));
            return false;
        }
        return true;
    }

    /************************************************************
     * Team 생성
     ************************************************************/
    @Click(R.id.btn_getting_started)
    void showTeamTeamCreationFragment() {
        String email = editTextLoginId.getText().toString();
        DialogFragment newFragment = TeamCreationDialogFragment.newInstance(email);
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
            log.error("createTeamInBackground", e);
            createTeamFailed(R.string.err_team_creation_failed);

        } catch (Exception e) {
            log.error("createTeamInBackground", e);
            createTeamFailed(R.string.err_network);
        }
    }

    @UiThread
    void createTeamSucceed() {
        mProgressWheel.dismiss();
        ColoredToast.showLong(getActivity(), getString(R.string.jandi_team_creation_succeed));
        setReadFlagForTutorial();
        getActivity().finish();
    }

    @UiThread
    void createTeamFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(getActivity(), getString(errMessageResId));
    }

    @SupposeUiThread
    void setReadFlagForTutorial() {
        JandiPreference.setFlagForTutorial(getActivity(), true);
    }

    /************************************************************
     * Team List 획득
     ************************************************************/
    @Click(R.id.btn_intro_action_signin_start)
    void startLogin() {
        if (buttonSignInStart.isSelected()) {
            imm.hideSoftInputFromWindow(editTextLoginId.getWindowToken(), 0);

            mProgressWheel.show();
            getTeamListInBackground(editTextLoginId.getEditableText().toString());
        }
    }

    @Background
    void getTeamListInBackground(String myEmailId) {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        try {
            // 나의 팀 ID 획득
            ResMyTeam resMyTeam = mJandiAuthClient.getMyTeamId(myEmailId);
            if (resMyTeam.teamList.size() > 0) {
                getTeamListSucceed(myEmailId, resMyTeam);
            } else {
                getTeamListFailed(errStringResNotRegisteredId);
            }
            return;
        } catch (JandiNetworkException e) {
            int errorStringRes = R.string.err_network;
            if (e.errCode == JandiNetworkException.DATA_NOT_FOUND) {
                // 팀이 아무것도 없는 사용자일 경우
                errorStringRes = errStringResNotRegisteredId;
            }
            getTeamListFailed(errorStringRes);
        } catch (Exception e) {
            log.error(e.toString(), e);
            getTeamListFailed(R.string.err_network);
        }
    }

    @UiThread
    void getTeamListSucceed(String myEmailId, ResMyTeam resMyTeam) {
        mProgressWheel.dismiss();
        setReadFlagForTutorial();
        try {
            String jsonExtraTeamList = convertPojoToJson(resMyTeam);
            moveToTeamSelectionActivity(myEmailId, jsonExtraTeamList);
        } catch (IOException e) {
            ColoredToast.showError(getActivity(), "");
        }
    }

    @UiThread
    void getTeamListFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(getActivity(), getString(errMessageResId));
    }

    @SupposeUiThread
    void moveToTeamSelectionActivity(String myEmailId, String jsonExtraTeamList) {
        TeamSelectionActivity_.intent(getActivity())
                .recievedEmail(myEmailId)
                .jsonExtraTeamList(jsonExtraTeamList)
                .start();
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private String convertPojoToJson(ResMyTeam myTeamList) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(myTeamList);
    }
}
