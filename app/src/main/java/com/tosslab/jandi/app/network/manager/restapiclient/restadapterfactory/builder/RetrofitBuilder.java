package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.ResponseConverter;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.RestAdapterDecor;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp.LoggingAppender;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp.StethoAppender;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;

/**
 * Created by Steve SeongUg Jung on 15. 6. 22..
 */
public class RetrofitBuilder {

    private static RetrofitBuilder retrofitBuilder;

    private Retrofit retrofit;

    private RetrofitBuilder() {}

    synchronized public static RetrofitBuilder newInstance() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new RetrofitBuilder();
        }
        return retrofitBuilder;
    }

    private Retrofit getRestAdapter() {

        if (retrofit == null) {
            retrofit = initRetrofit();
        }
        return retrofit;
    }

    private Retrofit initRetrofit() {
        List<RestAdapterDecor> restAdapterDecors = new ArrayList<>();
        restAdapterDecors.add(new ResponseConverter());

        Retrofit.Builder retofitBuilder = new Retrofit.Builder()
                .baseUrl(JandiConstantsForFlavors.SERVICE_INNER_API_URL);


        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .header(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                            .header("User-Agent", UserAgentUtil.getDefaultUserAgent())
                            .build();
                    return chain.proceed(newRequest);
                });

        try {
            okhttpClientBuilder.sslSocketFactory(createSslSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        LoggingAppender.add(okhttpClientBuilder);
        StethoAppender.add(okhttpClientBuilder);

        retofitBuilder.client(okhttpClientBuilder.build());
        for (RestAdapterDecor restAdapterDecor : restAdapterDecors) {
            retofitBuilder = restAdapterDecor.addRestAdapterProperty(retofitBuilder);
        }
        return retofitBuilder.build();
    }

    private SSLSocketFactory createSslSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }
        }, new SecureRandom());
        return context.getSocketFactory();
    }

    synchronized public <CLIENT> CLIENT create(Class<CLIENT> clazz) {
        return getRestAdapter().create(clazz);
    }
}
