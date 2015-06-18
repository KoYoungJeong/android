package com.tosslab.jandi.app.network.client;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
import com.tosslab.jandi.app.ui.intro.model.TestAccountInfoService;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import retrofit.RestAdapter;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
@EBean
public class JandiAuthClient {

    @RootContext
    Context context;

    @AfterInject
    void initAuthentication() {
    }


//    public ResConfig getConfig() throws JandiNetworkException {
//        try {
//            LogUtil.d("JandiAuthClient생성에서 jandiRestClient 생성");
//            JandiRestClient_ jandiRestClient = new JandiRestClient_(context);
//            jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//            return jandiRestClient.getConfig();
//        } catch (HttpStatusCodeException e) {
//            throw new JandiNetworkException(e);
//        } catch (Exception e) {
//            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
//        }
//    }

    public ResConfig getConfig() throws JandiNetworkException {
        try {
            JacksonConverter converter = new JacksonConverter(new ObjectMapper());

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setConverter(converter)
                    .setEndpoint("http://i2.jandi.io:8888/inner-api")
                    .build();

            return restAdapter.create(JandiRestV2Client.class).getConfig();

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        } catch (Exception e) {
            throw new JandiNetworkException(new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}