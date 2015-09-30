package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by tonyjs on 15. 9. 15..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketLinkPreviewThumbnailEvent {
    private String event;
    private int version;
    private Data data;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
        private int teamId;
        private int messageId;
        private ResMessages.LinkPreview linkPreview;

        public Size getSize() {
            return size;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public int getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
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
