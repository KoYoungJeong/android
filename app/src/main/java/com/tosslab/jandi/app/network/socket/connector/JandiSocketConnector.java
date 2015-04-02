package com.tosslab.jandi.app.network.socket.connector;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import java.net.URISyntaxException;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public class JandiSocketConnector implements SocketConnector {

    private Socket socket;

    public JandiSocketConnector() {
    }

    @Override
    public Emitter connect(String url, EventListener connectEventListener) {
        if (socket != null && socket.connected()) {
            return socket;
        }

        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = true;

                socket = IO.socket(url, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("INFO", "Success!!! hahahah");
                    connectEventListener.callback(args);
                }
            });
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("INFO", "Fail!!! hahahah");
                }
            });
            socket.connect();
        }

        return socket;
    }

    @Override
    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.connected();
    }

}
