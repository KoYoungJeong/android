package com.tosslab.jandi.app.network.client.conference_call;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.decor.JacksonConverter;
import com.tosslab.jandi.app.network.models.ReqGooroomeOtp;
import com.tosslab.jandi.app.network.models.ResGooroomeOtp;

import java.io.IOException;
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
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by tee on 2017. 6. 7..
 */

public class ConferenceCallApi {

    private static ConferenceCallApi conferenceCallApi;

    private ConferenceCallApi() {

    }

    public static ConferenceCallApi get() {
        if (conferenceCallApi == null) {
            conferenceCallApi = new ConferenceCallApi();
        }
        return conferenceCallApi;
    }

    private static SSLSocketFactory createSslSocketFactory()
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

    public ResGooroomeOtp getGooroomeOtp(ReqGooroomeOtp reqGooroomeOtp) throws RetrofitException {
        Retrofit.Builder retofitBuilder = new Retrofit.Builder()
                .baseUrl("https://api.gooroomee.com/")
                .addConverterFactory(JacksonConverter.create());

        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder();

        try {
            okhttpClientBuilder.sslSocketFactory(createSslSocketFactory())
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        OkHttpClient okHttpClient = okhttpClientBuilder.build();
        retofitBuilder.client(okHttpClient);

        Retrofit retrofit = retofitBuilder.build();

        ResGooroomeOtp resGooroomeOtp = null;
        Api api = retrofit.create(Api.class);
        Call<ResGooroomeOtp> call = api.getGooroomeOtp(reqGooroomeOtp);
        try {
            resGooroomeOtp = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resGooroomeOtp;
    }

    interface Api {
        @POST("gooroomee/outer/api/v1/room/user/otp")
        Call<ResGooroomeOtp> getGooroomeOtp(@Body ReqGooroomeOtp reqGooroomeOtp);
    }

}
