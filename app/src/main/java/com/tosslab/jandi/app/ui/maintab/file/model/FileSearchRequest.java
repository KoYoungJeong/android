package com.tosslab.jandi.app.ui.maintab.file.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 14. 12. 22..
 */
public class FileSearchRequest implements Request<ResSearchFile> {

    private final Context context;
    private final JandiRestClient jandiRestClient;
    private final ReqSearchFile reqSearchFile;

    private FileSearchRequest(Context context, JandiRestClient jandiRestClient, ReqSearchFile reqSearchFile) {
        this.context = context;
        this.jandiRestClient = jandiRestClient;
        this.reqSearchFile = reqSearchFile;
    }

    public static FileSearchRequest create(Context context, ReqSearchFile reqSearchFile) {
        return new FileSearchRequest(context, new JandiRestClient_(context), reqSearchFile);
    }


    @Override
    public ResSearchFile request() throws JandiNetworkException {

        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return jandiRestClient.searchFile(reqSearchFile);
    }
}
