package com.tosslab.jandi.app.network.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vimeo.stag.generated.Stag;

public class JsonMapper {
    private static JsonMapper jsonMapper;

    private ObjectMapper objectMapper;
    private Gson gson;

    private JsonMapper() {
        objectMapper = new ObjectMapper();
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new Stag.Factory())
                .create();
    }

    synchronized public static JsonMapper getInstance() {
        if (jsonMapper == null) {
            jsonMapper = new JsonMapper();
        }

        return jsonMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Gson getGson() {
        return gson;
    }
}
