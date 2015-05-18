package com.tosslab.jandi.app.services.socket;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.hasbinary.HasBinary;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.On;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.parser.Packet;
import com.github.nkzawa.socketio.parser.Parser;
import com.github.nkzawa.thread.EventThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by tonyjs on 15. 5. 18..
 */
public class JandiSocket extends Emitter {
    public static final String TAG = JandiSocket.class.getSimpleName();

    public static final String EVENT_CONNECT = "connect";
    public static final String EVENT_DISCONNECT = "disconnect";
    public static final String EVENT_ERROR = "error";

    public static final String EVENT_MESSAGE = "message";
    String id;

    private volatile boolean connected;
    private int ids;
    private String nsp;
    private Manager io;
    private Map<Integer, Ack> acks = new HashMap<Integer, Ack>();
    private Queue<On.Handle> subs;
    private final Queue<List<Object>> receiveBuffer = new LinkedList<List<Object>>();
    private final Queue<Packet<JSONArray>> sendBuffer = new LinkedList<Packet<JSONArray>>();

    private boolean encoding;
    private Parser.Encoder encoder;


    public JandiSocket(Manager io, String nsp) {
        this.io = io;
        this.nsp = nsp;
    }

    /**
     * Connects the socket.
     */
    public JandiSocket connect() {
        return this.open();
    }

    /**
     * Connects the socket.
     */
    public JandiSocket open() {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (JandiSocket.this.connected) return;

//                JandiSocket.this.subEvents();
                JandiSocket.this.io.open(); // ensure open
//                if (Manager.ReadyState.OPEN == JandiSocket.this.io.readyState) JandiSocket.this.onopen();
            }
        });
        return this;
    }

    /**
     * Send messages.
     *
     * @param args data to send.
     * @return a reference to this object.
     */
    public JandiSocket send(final Object... args) {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                JandiSocket.this.emit(EVENT_MESSAGE, args);
            }
        });
        return this;
    }

    /**
     * Emits an event. When you pass {@link Ack} at the last argument, then the acknowledge is done.
     *
     * @param event an event name.
     * @param args data to send.
     * @return a reference to this object.
     */
    @Override
    public Emitter emit(final String event, final Object... args) {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                List<Object> _args = new ArrayList<Object>(args.length + 1);
                _args.add(event);
                _args.addAll(Arrays.asList(args));

                JSONArray jsonArgs = new JSONArray();
                for (Object arg : _args) {
                    jsonArgs.put(arg);
                }
                int parserType = HasBinary.hasBinary(jsonArgs) ? Parser.BINARY_EVENT : Parser.EVENT;
                Packet<JSONArray> packet = new Packet<JSONArray>(parserType, jsonArgs);

                if (_args.get(_args.size() - 1) instanceof Ack) {
                    JandiSocket.this.acks.put(JandiSocket.this.ids, (Ack)_args.remove(_args.size() - 1));
                    jsonArgs = remove(jsonArgs, jsonArgs.length() - 1);
                    packet.data = jsonArgs;
                    packet.id = JandiSocket.this.ids++;
                }

                if (JandiSocket.this.connected) {
                    JandiSocket.this.packet(packet);
                } else {
                    JandiSocket.this.sendBuffer.add(packet);
                }
            }
        });
        return this;
    }

    private static JSONArray remove(JSONArray a, int pos) {
        JSONArray na = new JSONArray();
        for (int i = 0; i < a.length(); i++){
            if (i != pos) {
                Object v;
                try {
                    v = a.get(i);
                } catch (JSONException e) {
                    v = null;
                }
                na.put(v);
            }
        }
        return na;
    }

    private void packet(Packet packet) {
        packet.nsp = this.nsp;
        final JandiSocket self = this;

        if (!self.encoding) {
            self.encoding = true;
            this.encoder.encode(packet, new Parser.Encoder.Callback() {
                @Override
                public void call(Object[] encodedPackets) {
                    for (Object packet : encodedPackets) {
                        if (packet instanceof String) {
//                            self.engine.write((String)packet);
                        } else if (packet instanceof byte[]) {
//                            self.engine.write((byte[])packet);
                        }
                    }
                    self.encoding = false;
//                    self.processPacketQueue();
                }
            });
        } else {
//            self.packetBuffer.add(packet);
        }
    }

    private void onopen() {
        if (!"/".equals(this.nsp)) {
            this.packet(new Packet(Parser.CONNECT));
        }
    }

    private void onack(Packet<JSONArray> packet) {
        Ack fn = this.acks.remove(packet.id);
        fn.call(toArray(packet.data));
    }

    private void onconnect() {
        this.connected = true;
        this.emit(EVENT_CONNECT);
        this.emitBuffered();
    }

    private Ack ack(final int id) {
        final JandiSocket self = this;
        final boolean[] sent = new boolean[] {false};
        return new Ack() {
            @Override
            public void call(final Object... args) {
                EventThread.exec(new Runnable() {
                    @Override
                    public void run() {
                        if (sent[0]) return;
                        sent[0] = true;

                        int type = HasBinary.hasBinary(args) ? Parser.BINARY_ACK : Parser.ACK;
                        Packet<JSONArray> packet = new Packet<JSONArray>(type, new JSONArray(Arrays.asList(args)));
                        packet.id = id;
                        self.packet(packet);
                    }
                });
            }
        };
    }

    private void onevent(Packet<JSONArray> packet) {
        List<Object> args = new ArrayList<Object>(Arrays.asList(toArray(packet.data)));

        if (packet.id >= 0) {
            args.add(this.ack(packet.id));
        }

        if (this.connected) {
            if (args.size() == 0) return;
            String event = args.remove(0).toString();
            super.emit(event, args.toArray());
        } else {
            this.receiveBuffer.add(args);
        }
    }

    private void emitBuffered() {
        List<Object> data;
        while ((data = this.receiveBuffer.poll()) != null) {
            String event = (String)data.get(0);
            super.emit(event, data.toArray());
        }
        this.receiveBuffer.clear();

        Packet<JSONArray> packet;
        while ((packet = this.sendBuffer.poll()) != null) {
            this.packet(packet);
        }
        this.sendBuffer.clear();
    }

    private void ondisconnect() {
        this.destroy();
        this.onclose("io server disconnect");
    }

    private void destroy() {
        if (this.subs != null) {
            // clean subscriptions to avoid reconnection
            for (On.Handle sub : this.subs) {
                sub.destroy();
            }
            this.subs = null;
        }

//        this.io.destroy(this);
    }

    private void onclose(String reason) {
        this.connected = false;
        this.id = null;
        this.emit(EVENT_DISCONNECT, reason);
    }

    private void onpacket(Packet packet) {
        if (!this.nsp.equals(packet.nsp)) return;

        switch (packet.type) {
            case Parser.CONNECT:
                this.onconnect();
                break;

            case Parser.EVENT:
                this.onevent(packet);
                break;

            case Parser.BINARY_EVENT:
                this.onevent(packet);
                break;

            case Parser.ACK:
                this.onack(packet);
                break;

            case Parser.BINARY_ACK:
                this.onack(packet);
                break;

            case Parser.DISCONNECT:
                this.ondisconnect();
                break;

            case Parser.ERROR:
                this.emit(EVENT_ERROR, packet.data);
                break;
        }
    }

    /**
     * Disconnects the socket.
     *
     * @return a reference to this object.
     */
    public JandiSocket close() {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (JandiSocket.this.connected) {
                    JandiSocket.this.packet(new Packet(Parser.DISCONNECT));
                }

                JandiSocket.this.destroy();

                if (JandiSocket.this.connected) {
                    JandiSocket.this.onclose("io client disconnect");
                }
            }
        });
        return this;
    }

    /**
     * Disconnects the socket.
     *
     * @return a reference to this object.
     */
    public JandiSocket disconnect() {
        return this.close();
    }

    public Manager io() {
        return this.io;
    }

    public boolean connected() {
        return this.connected;
    }

    /**
     * A property on the socket instance that is equal to the underlying engine.io socket id.
     *
     * The value is present once the socket has connected, is removed when the socket disconnects and is updated if the socket reconnects.
     *
     * @return a socket id
     */
    public String id() {
        return this.id;
    }

    private static Object[] toArray(JSONArray array) {
        int length = array.length();
        Object[] data = new Object[length];
        for (int i = 0; i < length; i++) {
            Object v;
            try {
                v = array.get(i);
            } catch (JSONException e) {
                v = null;
            }
            data[i] = v == JSONObject.NULL ? null : v;
        }
        return data;
    }

}
