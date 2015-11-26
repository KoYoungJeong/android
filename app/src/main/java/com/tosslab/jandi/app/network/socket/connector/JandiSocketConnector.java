package com.tosslab.jandi.app.network.socket.connector;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public class JandiSocketConnector implements SocketConnector {
    public static final String TAG = "SocketConnector";
    private Socket socket;
    private Status status = Status.READY;

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
                updateEventHistory();
            }).on(Socket.EVENT_ERROR, args -> {
                LogUtil.e(TAG, Socket.EVENT_ERROR);
                disconnectCallback(disconnectListener, args);
                JandiPreference.setSocketConnectedLastTime();
            }).on(Socket.EVENT_DISCONNECT, args -> {
                LogUtil.e(TAG, Socket.EVENT_DISCONNECT);
                disconnectCallback(disconnectListener, args);
                JandiPreference.setSocketConnectedLastTime();
            }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                LogUtil.e(TAG, Socket.EVENT_CONNECT_ERROR);
                disconnectCallback(disconnectListener, args);
                JandiPreference.setSocketConnectedLastTime();
            }).on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                LogUtil.e(TAG, Socket.EVENT_CONNECT_TIMEOUT);
                disconnectCallback(disconnectListener, args);
                JandiPreference.setSocketConnectedLastTime();
            });
            socket.connect();
        }

        return socket;
    }

    // 확장성 생각하여 추후 모듈로 빼내야 함.
    private void updateEventHistory() {
        long ts = JandiPreference.getSocketConnectedLastTime();
        EntityManager entityManager = EntityManager.getInstance();
        int userId = entityManager.getMe().getId();

        if (ts != -1) {
            try {
                ResEventHistory eventHistory =
                        RequestApiManager.getInstance().getEventHistory(ts, userId, "file_unshared", null);
                Iterator<ResEventHistory.EventHistoryInfo> i = eventHistory.records.iterator();
                while (i.hasNext()) {
                    ResEventHistory.EventHistoryInfo eventInfo = i.next();
                    if (eventInfo instanceof SocketFileUnsharedEvent) {
                        SocketFileUnsharedEvent event = (SocketFileUnsharedEvent) eventInfo;
                        int fileId = event.getFile().getId();
                        int roomId = event.room.id;
                        MessageRepository.getRepository().updateUnshared(fileId, roomId);
                    }
                }
            } catch (RetrofitError e) {
                JandiPreference.setSocketConnectedLastTime();
                e.printStackTrace();
                // 오류 나면 그 때 부터 데이터가 꼬이기 시작한다. 다 지우기 - 최선인가.....
                OrmDatabaseHelper dbHelper =
                        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
                dbHelper.clearAllData();

            }
        } else {
            JandiPreference.setSocketConnectedLastTime();
            // 캐시 다 지우기
            OrmDatabaseHelper dbHelper =
                    OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            dbHelper.clearAllData();
        }

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
        if (socket != null && socket.connected()) {
            status = Status.DISCONNECTING;
            socket.off();
            socket.disconnect();

            while (socket.connected()) {
                try {
                    Thread.sleep(100);
                    LogUtil.d(TAG, "Socket Stopping...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //FIXME NullPointerException 여지가 있을지?
            socket = null;
            status = Status.READY;
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
