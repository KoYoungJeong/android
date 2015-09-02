package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 8. 25..
 */
public class ReqUpdateFolder {

    public UpdateItems updateItems;

    public ReqUpdateFolder() {
        this.updateItems = new UpdateItems();
    }

    @Override
    public String toString() {
        return "ReqUpdateFolder{" +
                "updateItems=" + updateItems.toString() +
                '}';
    }

    public class UpdateItems {
        private String name;
        private int seq;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        @Override
        public String toString() {
            return "UpdateItems{" +
                    "name='" + name + '\'' +
                    ", seq=" + seq +
                    '}';
        }
    }
}
