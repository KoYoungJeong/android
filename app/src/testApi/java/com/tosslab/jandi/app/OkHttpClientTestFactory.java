package com.tosslab.jandi.app;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp.LoggingAppender;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp.StethoAppender;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by tonyjs on 16. 4. 19..
 */
public class OkHttpClientTestFactory {

    public static final String USERID = "ekuvekez-9240@yopmail.com";
    public static final String PASSWORD = "1234asdf";
    static String token = "";

    public static void init() {

        if (JandiApplication.okHttpClient == null) {

            OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder()
                    .addNetworkInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .header(JandiConstants.AUTH_HEADER, token)
                                .header("User-Agent", UserAgentUtil.getDefaultUserAgent())
                                .build();
                        return chain.proceed(newRequest);
                    });

            try {
                okhttpClientBuilder.sslSocketFactory(createSslSocketFactory());
                okhttpClientBuilder.readTimeout(60, TimeUnit.SECONDS);
                okhttpClientBuilder.writeTimeout(60, TimeUnit.SECONDS);
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            LoggingAppender.add(okhttpClientBuilder);
            StethoAppender.add(okhttpClientBuilder);
            OkHttpClient okHttpClient = okhttpClientBuilder.build();
            JandiApplication.okHttpClient = okHttpClient;
            HttpLoggingInterceptor.Logger.DEFAULT.log("Set OkHttp!!!!");

            try {
                HashMap<String, Object> body = new HashMap<>();
                body.put("grant_type", "password");
                body.put("username", USERID);
                body.put("password", PASSWORD);
                ResAccessToken accessToken = RetrofitBuilder.getInstance().create(LoginApi.class).getAccessToken(body).execute().body();
                token = String.format("%s %s", accessToken.getTokenType(), accessToken.getAccessToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    interface LoginApi {
        // 로그인
        @POST("token")
        @Headers({"Content-Type : application/json",
                "Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V2})
        Call<ResAccessToken> getAccessToken(@Body Map<String, Object> body);


    }

}
