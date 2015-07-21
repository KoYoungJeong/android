package com.tosslab.jandi.app.network.jackson.deserialize.leftsidemenu;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class PrivateTopicRefDeserialize extends JsonDeserializer<ResLeftSideMenu.PrivateTopicRef> {
    @Override
    public ResLeftSideMenu.PrivateTopicRef deserialize(JsonParser jp, DeserializationContext ctxt) throws
            IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);

        ResLeftSideMenu.PrivateTopicRef entityRef = new ResLeftSideMenu.PrivateTopicRef();
        entityRef.value = root.getIntValue();

        return entityRef;
    }
}
