package com.tosslab.jandi.lib.sprinkler.io;

import com.squareup.okhttp.OkHttpClient;
import com.tosslab.jandi.lib.sprinkler.Logger;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.client.OkClient;

/**
 * Created by tonyjs on 15. 11. 23..
 */
final class OkConnectionClient extends OkClient {
    private static final String TAG = "OkConnectionClient";

    private static final int CONNECTION_TIMEOUT = 7 * 1000;
    private static final int READ_TIMEOUT = 7 * 1000;

    public OkConnectionClient() {
        this(getDefaultClient());
    }

    public OkConnectionClient(OkHttpClient client) {
        super(client);
        Logger.i(TAG, String.format(
                "initialize(connectionTimeOut:%s, readTimeOut:%d)",
                client.getConnectTimeout(), client.getReadTimeout()));
    }

    private static OkHttpClient getDefaultClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();
        if (sslSocketFactory != null) {
            client.setSslSocketFactory(sslSocketFactory);
        }
        return client;
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
