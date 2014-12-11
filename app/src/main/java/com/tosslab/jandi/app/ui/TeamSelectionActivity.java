package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.team.TeamListAdapter;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResAccessToken;
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
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by justinygchoi on 14. 11. 14..
 */
@EActivity(R.layout.activity_team_selection)
public class TeamSelectionActivity extends Activity {
    private final Logger log = Logger.getLogger(TeamSelectionActivity.class);

    @Extra
    String recievedEmail;
    @Extra
    String jsonExtraTeamList;

    @RestService
    JandiRestClient mJandiRestClient;

    private int mSelectedTeamId;
    private JandiAuthClient mJandiAuthClient;

    @ViewById(R.id.lv_intro_team_list)
    ListView listViewTeamList;
    @ViewById(R.id.et_intro_signin_password)
    EditText editTextPassword;
    @ViewById(R.id.view_intro_inactive_mask)
    View viewInactiveMask;
    @ViewById(R.id.btn_intro_action_signin)
    Button buttonSignIn;

    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;

    @AfterViews
    void init() {
        setUpActionBar();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 로그인 관련 Network Client 설정
        mJandiAuthClient = new JandiAuthClient(mJandiRestClient);

        setActivationColorForButton();
        View header = getLayoutInflater().inflate(R.layout.item_team_list_title, null, false);
        listViewTeamList.addHeaderView(header);
        try {
            final TeamListAdapter adapter = getTeamListAdapter(jsonExtraTeamList);
            listViewTeamList.setAdapter(adapter);
            listViewTeamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position == 0) {
                        // Index of TITLE. DO NOTHING
                        return;
                    }
                    viewInactiveMask.setVisibility(View.GONE);
                    listViewTeamList.setSelection(position);

                    editTextPassword.setFocusableInTouchMode(true);
                    editTextPassword.setFocusable(true);

                    ResMyTeam.Team selectedMyTeam = adapter.getItem(position - 1);
                    mSelectedTeamId = selectedMyTeam.teamId;
                    log.debug(selectedMyTeam.name + ", id=" + mSelectedTeamId + ", is selected");
                }
            });
        } catch (IOException e) {
            ColoredToast.showError(this, getString(R.string.err_service_connection));
            finish();
        }
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

    private TeamListAdapter getTeamListAdapter(String jsonTeamList) throws IOException {
        ResMyTeam myTeamList = convertJsonToPojo(jsonTeamList);
        List<ResMyTeam.Team> myTeams = myTeamList.teamList;
        return new TeamListAdapter(this, myTeams);
    }

    private ResMyTeam convertJsonToPojo(String jsonTeamList) throws IOException {
        log.debug("JSON Extra : " + jsonTeamList);
        return new ObjectMapper().readValue(jsonTeamList, ResMyTeam.class);
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private void setActivationColorForButton() {
        // 텍스트에 글이 있으면 버튼 색상 변경
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (FormatConverter.isInvalidPasswd(editable.toString())) {
                    buttonSignIn.setSelected(false);
                    buttonSignIn.setEnabled(false);
                } else {
                    buttonSignIn.setSelected(true);
                    buttonSignIn.setEnabled(true);
                }
            }
        });
    }


    /**
     * *********************************************************
     * 로그인 수행
     * **********************************************************
     */
    @Click(R.id.btn_intro_action_signin)
    void pressLoginButton() {
        hideSoftKeyboard();
        mProgressWheel.show();
        doLoginInBackground(editTextPassword.getEditableText().toString());
    }

    private void hideSoftKeyboard() {
        imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
    }

    @Background
    void doLoginInBackground(String passwd) {
        try {
            ResAccessToken resAuthToken = mJandiAuthClient.login(mSelectedTeamId, recievedEmail, passwd);

            if (resAuthToken != null) {
                doLoginSucceed(resAuthToken);
            } else {
                doLoginFailed(R.string.err_login);
            }
        } catch (JandiNetworkException e) {
            if (e.errCode == JandiNetworkException.INVALID_PASSWD) {
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
    void doLoginSucceed(ResAccessToken token) {
        mProgressWheel.dismiss();
        String myAccessToken = token.getAccessToken();
        JandiPreference.setMyToken(this, myAccessToken);
        moveToMainActivity();
    }

    @UiThread
    void doLoginFailed(int errMessageResId) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, getString(errMessageResId));
    }

    @SupposeUiThread
    void moveToMainActivity() {
        // MainActivity 이동
        MainTabActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        finish();
    }
}
