package com.tosslab.jandi.app.push.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = PushInfo.class,
        property = "push_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MarkerPushInfo.class, name = "marker_updated"),
        @JsonSubTypes.Type(value = PushInfo.class, name = "message_created"),
        @JsonSubTypes.Type(value = PushInfo.class, name = "comment_created"),
        @JsonSubTypes.Type(value = PushInfo.class, name = "file_shared")})
public class BasePushInfo {
    @JsonProperty("push_type")
    private String pushType;

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

}
