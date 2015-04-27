package com.tosslab.jandi.app.network.spring;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by Steve SeongUg Jung on 15. 4. 13..
 */
public class JacksonMapper {
    private static JacksonMapper jacksonMapper;

    private ObjectMapper objectMapper;

    private JacksonMapper() {
        objectMapper = new ObjectMapper();
    }

    public static JacksonMapper getInstance() {
        if (jacksonMapper == null) {
            jacksonMapper = new JacksonMapper();
        }

        return jacksonMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
