package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqCreateTeam;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Locale;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
@EBean
public class JandiAuthClient {

    @RestService
    JandiRestClient mRestClient;

    public ResConfig getConfig() throws JandiNetworkException {
        try {
            return mRestClient.getConfig();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon createTeam(String email) throws JandiNetworkException {
        try {
            String languageCode = getLanguageCode();
            ReqCreateTeam req = new ReqCreateTeam(email, languageCode);
            return mRestClient.createTeam(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    private String getLanguageCode() {
        String languageCode = Locale.getDefault().toString().toLowerCase();
        if (languageCode.startsWith("zh")) {
            // 중국어
            if (languageCode.equals("zh_tw")) {
                // 번체
                languageCode = "zh-tw";
            } else {
                // 간체
                languageCode = "zh-cn";
            }
        } else if (languageCode.startsWith("ko")) {
            // 한글
            languageCode = "ko";
        } else if (languageCode.startsWith("ja")) {
            // 일본어
            languageCode = "ja";
        } else {
            // 기본 (영어)
            languageCode = "en";
        }
        return languageCode;
    }

    public ResMyTeam getMyTeamId(String id) throws JandiNetworkException {
        try {
            return mRestClient.getTeamId(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResAccessToken login(int teamId, String id, String passwd) throws JandiNetworkException {
        try {
            mRestClient.setHeader("Content-Type", "application/json");
            return mRestClient.getAccessToken(ReqAccessToken.createPasswordReqToken(id, passwd));
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }
}
