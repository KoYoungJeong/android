package com.tosslab.jandi.app.ui.login.login.model;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class IntroLoginModel {

    private final Logger log = Logger.getLogger(IntroLoginModel.class);

    @RestService
    JandiRestClient jandiRestClient;

    private JandiAuthClient mJandiAuthClient;

    private Callback callback;

    @AfterInject
    void initObject() {
        mJandiAuthClient = new JandiAuthClient(jandiRestClient);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Background
    public void createTeamInBackground(String myEmailId) {
        try {
            // 나의 팀 ID 획득
            ResCommon res = mJandiAuthClient.createTeam(myEmailId);
            if (callback != null) {
                callback.onCreateTeamSuccess();
            }
        } catch (JandiNetworkException e) {
            log.error("createTeamInBackground", e);
            createTeamFail(R.string.err_team_creation_failed);
        } catch (Exception e) {
            log.error("createTeamInBackground", e);
            createTeamFail(R.string.err_network);
        }
    }

    private void createTeamFail(int errorStringResId) {
        if (callback != null) {
            callback.onCreateTeamFail(errorStringResId);
        }
    }

    @Background
    public void getTeamListInBackground(String myEmailId) {
        // 팀이 아무것도 없는 사용자일 경우의 에러 메시지
        final int errStringResNotRegisteredId = R.string.err_login_unregistered_id;

        try {
            // 나의 팀 ID 획득
            ResMyTeam resMyTeam = mJandiAuthClient.getMyTeamId(myEmailId);
            if (resMyTeam.teamList.size() > 0) {
                if (callback != null) {
                    callback.onGetTeamListSuccess(myEmailId, resMyTeam);
                }
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

    @SupposeUiThread
    public boolean isValidEmailFormat(String email) {
        // ID 입력의 포멧 체크
        if (FormatConverter.isInvalidEmailString(email)) {
            return false;
        }
        return true;
    }

    private void getTeamListFailed(int errStringResNotRegisteredId) {
        if (callback != null) {
            callback.onGetTeamListFail(errStringResNotRegisteredId);
        }
    }

    public interface Callback {

        void onCreateTeamSuccess();

        void onCreateTeamFail(int stringResId);

        void onGetTeamListSuccess(String myEmailId, ResMyTeam resMyTeam);

        void onGetTeamListFail(int errorStringResId);


    }
}
