package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.network.models.ReqCreateTeam;
import com.tosslab.jandi.app.network.models.ReqLogin;
import com.tosslab.jandi.app.network.models.ResAuthToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class JandiAuthClient {
    JandiRestClient mRestClient;

    public JandiAuthClient(JandiRestClient jandiRestClient) {
        mRestClient = jandiRestClient;
        mRestClient.setHeader("Accept", JandiV1HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);
    }

    public ResConfig getConfig() throws JandiNetworkException {
        try {
            return mRestClient.getConfig();
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResCommon createTeam(String email) throws JandiNetworkException {
        try {
            ReqCreateTeam req = new ReqCreateTeam();
            req.email = email;
            return mRestClient.createTeam(req);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResMyTeam getMyTeamId(String id) throws JandiNetworkException {
        try {
            return mRestClient.getTeamId(id);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public ResAuthToken login(int teamId, String id, String passwd) throws JandiNetworkException {
        ReqLogin reqLogin = new ReqLogin(teamId, id, passwd);
        try {
            return mRestClient.loginAndReturnToken(reqLogin);
        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }
}
