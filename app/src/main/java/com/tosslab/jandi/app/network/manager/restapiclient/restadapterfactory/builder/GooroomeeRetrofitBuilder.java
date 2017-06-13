package com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.JacksonConverter;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;

/**
 * Created by tee on 2017. 6. 13..
 */

public class GooroomeeRetrofitBuilder implements RetrofitBuilder {

    private static GooroomeeRetrofitBuilder retrofitBuilder;
    private final String url;

    private Retrofit retrofit;

    private GooroomeeRetrofitBuilder(String url) {
        this.url = url;
    }

    synchronized public static GooroomeeRetrofitBuilder getInstance() {
        if (retrofitBuilder == null) {
            retrofitBuilder = new GooroomeeRetrofitBuilder("https://api.gooroomee.com/");
        }
        return retrofitBuilder;
    }

    synchronized public static void reset() {
        retrofitBuilder = new GooroomeeRetrofitBuilder("https://api.gooroomee.com/");
    }

    private SSLSocketFactory createSslSocketFactory()
            throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
                    throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
                    throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }
        }, new SecureRandom());
        return context.getSocketFactory();
    }

    private Retrofit getRestAdapter() {
        if (retrofit == null) {
            retrofit = initRetrofit();
        }

        return retrofit;
    }

    private Retrofit initRetrofit() {
        Retrofit.Builder retofitBuilder = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverter.create());

        retofitBuilder.client(getOkHttpClient());
        return retofitBuilder.build();
    }

    private OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder();

        okhttpClientBuilder.addNetworkInterceptor(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader("X-GRM-AuthToken", "1ee7011f351a0d71540916d881112912706194221439118008");
            return chain.proceed(requestBuilder.build());
        });

        try {
            okhttpClientBuilder.sslSocketFactory(createSslSocketFactory())
                    .hostnameVerifier((hostname, session) -> true)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return okhttpClientBuilder.build();
    }

    synchronized public <CLIENT> CLIENT create(Class<CLIENT> clazz) {
        return getRestAdapter().create(clazz);
    }

}
