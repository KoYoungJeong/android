package com.tosslab.jandi.app.network.socket.connector;

import com.tosslab.jandi.app.network.DomainUtil;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public class JandiSocketConnector implements SocketConnector {
    public static final String TAG = "SocketConnector";
    private Socket socket;
    private Status status = Status.READY;
    private boolean isInDisconnecting = false;

    @Override
    public Emitter connect(String url, EventListener disconnectListener) {
        if (socket != null && socket.connected()) {
            return socket;
        }

        status = Status.CONNECTING;
        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = false;
                options.multiplex = false;
                options.forceNew = false;
                options.timeout = 1000 * 10;
                options.rememberUpgrade = true;
                options.transports = new String[]{WebSocket.NAME};
                options.hostnameVerifier = (hostname, session) -> {
                    for (String domain : DomainUtil.DOMAINS) {
                        if (hostname.contains(domain)) {
                            return true;
                        }
                    }
                    return false;
                };
                try {
                    options.sslContext = getSSLContext();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                socket = IO.socket(url, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, args -> {
                LogUtil.e(TAG, Socket.EVENT_CONNECT);
                status = Status.CONNECTED;
            }).on(Socket.EVENT_ERROR, args -> {
                LogUtil.e(TAG, Socket.EVENT_ERROR);
                disconnectCallback(disconnectListener, args);
            }).on(Socket.EVENT_DISCONNECT, args -> {
                LogUtil.e(TAG, Socket.EVENT_DISCONNECT);
                disconnectCallback(disconnectListener, args);
            }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                LogUtil.e(TAG, Socket.EVENT_CONNECT_ERROR);
                disconnectCallback(disconnectListener, args);
            }).on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                LogUtil.e(TAG, Socket.EVENT_CONNECT_TIMEOUT);
                disconnectCallback(disconnectListener, args);
            });
            socket.connect();
        }

        return socket;
    }

    private void disconnectCallback(EventListener disconnectListener, Object[] args) {
        status = Status.READY;
        if (args != null) {
            for (Object arg : args) {
                LogUtil.e(TAG, "Disconnect Reason : " + arg.toString());
            }
        }

        if (disconnectListener != null) {
            disconnectListener.callback(args);
        }
    }

    @Override
    public void disconnect() {
        if (isInDisconnecting) {
            return;
        }

        if (socket != null && socket.connected()) {
            isInDisconnecting = true;

            status = Status.DISCONNECTING;
            socket.off();
            socket.disconnect();

            Observable.just(1)
                    .doOnNext(integer -> {
                        while (socket.connected()) {
                            try {
                                Thread.sleep(100);
                                LogUtil.d(TAG, "Socket Stopping...");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(disconnected -> {
                        //FIXME NullPointerException 여지가 있을지?
                        socket = null;
                        status = Status.READY;
                        isInDisconnecting = false;
                    });
        }
    }

    @Override
    public boolean isConnectingOrConnected() {
        if (status == Status.READY || status == Status.DISCONNECTING) {
            return false;
        }
        boolean alreadyConnect = socket != null && socket.connected();
        return alreadyConnect || status == Status.CONNECTING || status == Status.CONNECTED;
    }

    private SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
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
        return context;
    }

    enum Status {
        READY, CONNECTING, CONNECTED, DISCONNECTING
    }
}
