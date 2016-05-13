package com.tosslab.jandi.app.push.to;

public enum PushRoomType {
    CHANNEL("channel"),
    PRIVATE_GROUP("privateGroup"),
    CHAT("chat");

    String name;

    PushRoomType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
