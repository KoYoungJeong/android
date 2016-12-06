package com.tosslab.jandi.app.network.manager.okhttp;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.DomainUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp.LoggingAppender;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.okhttp.StethoAppender;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;

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

/**
 * Created by tonyjs on 16. 4. 19..
 */
public class OkHttpClientFactory {

    public static OkHttpClient getOkHttpClient() {

        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .header(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                            .header("User-Agent", UserAgentUtil.getDefaultUserAgent())
                            .build();
                    return chain.proceed(newRequest);
                });

        try {
            okhttpClientBuilder.sslSocketFactory(createSslSocketFactory())
                    .hostnameVerifier((hostname, session) -> {
                        for (String domain : DomainUtil.DOMAINS) {
                            if (hostname.contains(domain)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        LoggingAppender.add(okhttpClientBuilder);
        StethoAppender.add(okhttpClientBuilder);
        return okhttpClientBuilder.build();
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

}
