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
public class TopicCreateInfoDeserializer extends JsonDeserializer<ResMessages.PublicCreateInfo.IntegerWrapper> {
    @Override
    public ResMessages.PublicCreateInfo.IntegerWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);
        ResMessages.PublicCreateInfo.IntegerWrapper integerWrapper = new ResMessages.PublicCreateInfo.IntegerWrapper();
        integerWrapper.setMemberId(root.getIntValue());
        return integerWrapper;
    }
}
