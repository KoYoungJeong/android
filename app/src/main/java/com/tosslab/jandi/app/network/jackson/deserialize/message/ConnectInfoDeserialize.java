package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ConnectInfoDeserialize extends JsonDeserializer<ResMessages.ConnectInfo> {
    @Override
    public ResMessages.ConnectInfo deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();

        ResMessages.ConnectInfo connectInfo = new ResMessages.ConnectInfo();
        JsonNode jsonNode = mapper.readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        Map.Entry<String, JsonNode> fiedEntries;
        while (fields.hasNext()) {
            fiedEntries = fields.next();
            String fieldKey = fiedEntries.getKey();
            if (TextUtils.equals(fieldKey, "event")) {
                connectInfo.event = fiedEntries.getValue().getTextValue();
            } else if (TextUtils.equals(fieldKey, "title")) {
                JsonNode value = fiedEntries.getValue();
                if (!value.isNull()) {
                    connectInfo.title = value.getTextValue();
                } else {
                    connectInfo.title = "null";
                }
            } else if (TextUtils.equals(fieldKey, "description")) {
                JsonNode value = fiedEntries.getValue();
                if (!value.isNull()) {
                    connectInfo.description = value.getTextValue();
                } else {
                    connectInfo.description = String.valueOf(null);
                }
            }
        }

        return connectInfo;
    }
}
