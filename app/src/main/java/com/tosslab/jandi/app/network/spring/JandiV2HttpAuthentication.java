package com.tosslab.jandi.app.network.spring;

import org.springframework.http.HttpAuthentication;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class JandiV2HttpAuthentication extends HttpAuthentication {

    private String tokenType;
    private String accessToken;

    public JandiV2HttpAuthentication(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    @Override
    public String getHeaderValue() {
        return String.format("%s %s", tokenType, accessToken);
    }
}
