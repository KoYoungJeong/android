package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class ReqPushToken {
    private final List<PushToken> tokens;

    public ReqPushToken(List<PushToken> tokens) {
        this.tokens = tokens;
    }

    public List<PushToken> getTokens() {
        return tokens;
    }

}
