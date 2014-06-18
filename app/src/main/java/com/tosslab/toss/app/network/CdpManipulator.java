package com.tosslab.toss.app.network;

/**
 * Created by justinygchoi on 2014. 6. 17..
 */
public class CdpManipulator {
    TossRestClient mRestClient;
    int mCdpType;

    public CdpManipulator(TossRestClient tossRestClient, int cdpType, String token) {
        mRestClient = tossRestClient;
        mRestClient.setHeader("Authorization", token);
    }
}
