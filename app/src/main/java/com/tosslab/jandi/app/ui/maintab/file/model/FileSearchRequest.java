package com.tosslab.jandi.app.ui.maintab.file.model;

import android.content.Context;

import com.tosslab.jandi.app.network.JacksonConverter;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.client.JandiRestV2Client;
import com.tosslab.jandi.app.network.client.chat.ChatApiV2Client;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 22..
 */
public class FileSearchRequest implements Request<ResSearchFile> {

    private final Context context;
//    private final JandiRestClient jandiRestClient;
    private final ReqSearchFile reqSearchFile;

    RestAdapter restAdapter;

//    private FileSearchRequest(Context context, JandiRestClient jandiRestClient, ReqSearchFile reqSearchFile) {
//        this.context = context;
//        this.jandiRestClient = jandiRestClient;
//        this.reqSearchFile = reqSearchFile;
//    }

    private FileSearchRequest(Context context, ReqSearchFile reqSearchFile) {
        this.context = context;
        this.reqSearchFile = reqSearchFile;

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(converter)
                .setEndpoint("http://i2.jandi.io:8888/inner-api")
                .build();
    }

    public static FileSearchRequest create(Context context, ReqSearchFile reqSearchFile) {
        return new FileSearchRequest(context, reqSearchFile);
    }


    @Override
    public ResSearchFile request() throws JandiNetworkException {

//        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//        return jandiRestClient.searchFile(reqSearchFile);
        return restAdapter.create(JandiRestV2Client.class).searchFile(reqSearchFile);
    }
}
