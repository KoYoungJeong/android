package com.tosslab.jandi.lib.sprinkler.io.flush;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by tonyjs on 15. 11. 23..
 */
final class OkConnectionClient {
    private static final String TAG = "OkConnectionClient";

    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final int READ_TIMEOUT = 60 * 1000;

    private OkConnectionClient() {
    }

    public static OkHttpClient getDefaultClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        clientBuilder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();
        if (sslSocketFactory != null) {
            clientBuilder.sslSocketFactory(sslSocketFactory);
        }
        return clientBuilder.build();
    }

    private static SSLSocketFactory getDefaultSSLSocketFactory() {
        SSLSocketFactory sslSocketFactory;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
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
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
        return sslSocketFactory;
    }
}
