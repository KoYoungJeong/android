package com.tosslab.jandi.app.network.jackson.deserialize.message;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.io.IOException;

public class InviteInfoDeserializer extends JsonDeserializer<ResMessages.InviteEvent.IntegerWrapper> {
    @Override
    public ResMessages.InviteEvent.IntegerWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);
        ResMessages.InviteEvent.IntegerWrapper integerWrapper = new ResMessages.InviteEvent.IntegerWrapper();
        integerWrapper.setInviteUserId(root.longValue());
        return integerWrapper;
    }
}
