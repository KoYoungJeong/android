package com.tosslab.jandi.app.network.jackson.deserialize.message;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.io.IOException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 22..
 */
public class LinkShareEntityDeserializer extends JsonDeserializer<ResMessages.OriginalMessage.IntegerWrapper> {
    @Override
    public ResMessages.OriginalMessage.IntegerWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);
        ResMessages.OriginalMessage.IntegerWrapper integerWrapper = new ResMessages.OriginalMessage.IntegerWrapper();
        integerWrapper.setShareEntity(root.longValue());
        return integerWrapper;
    }
}
