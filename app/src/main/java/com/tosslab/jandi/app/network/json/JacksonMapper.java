package com.tosslab.jandi.app.network.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonMapper {
    private static JacksonMapper jacksonMapper;

    private ObjectMapper objectMapper;

    private JacksonMapper() {
        objectMapper = new ObjectMapper();
    }

    synchronized public static JacksonMapper getInstance() {
        if (jacksonMapper == null) {
            jacksonMapper = new JacksonMapper();
        }

        return jacksonMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
