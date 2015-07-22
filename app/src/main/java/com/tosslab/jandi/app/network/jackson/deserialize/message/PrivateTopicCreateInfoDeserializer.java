package com.tosslab.jandi.app.network.jackson.deserialize.message;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 22..
 */
public class PrivateTopicCreateInfoDeserializer extends JsonDeserializer<ResMessages.PrivateCreateInfo.IntegerWrapper> {
    @Override
    public ResMessages.PrivateCreateInfo.IntegerWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);
        ResMessages.PrivateCreateInfo.IntegerWrapper integerWrapper = new ResMessages.PrivateCreateInfo.IntegerWrapper();
        integerWrapper.setMemberId(root.getIntValue());
        return integerWrapper;
    }
}
