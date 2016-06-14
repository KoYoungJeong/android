package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by tonyjs on 15. 9. 15..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@Version(1)
public class SocketLinkPreviewThumbnailEvent implements EventHistoryInfo {
    private String event;
    private int version;
    private long teamId;
    private long ts;
    private Data data;

    @Override
    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }


    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketLinkPreviewThumbnailEvent{" +
                "event='" + event + '\'' +
                ", version=" + version +
                ", data=" + data +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private Size size;
        private long teamId;
        private long messageId;
        private ResMessages.LinkPreview linkPreview;

        public Size getSize() {
            return size;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public long getTeamId() {
            return teamId;
        }

        public void setTeamId(long teamId) {
            this.teamId = teamId;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public ResMessages.LinkPreview getLinkPreview() {
            return linkPreview;
        }

        public void setLinkPreview(ResMessages.LinkPreview linkPreview) {
            this.linkPreview = linkPreview;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "size=" + size +
                    ", messageId=" + messageId +
                    ", linkPreview=" + linkPreview +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public static class Size {
            private int width;
            private int height;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            @Override
            public String toString() {
                return "Size{" +
                        "width=" + width +
                        ", height=" + height +
                        '}';
            }
        }

    }


}
