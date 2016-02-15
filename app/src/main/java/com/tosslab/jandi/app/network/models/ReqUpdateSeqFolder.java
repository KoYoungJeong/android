package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 16. 1. 25..
 */
public class ReqUpdateSeqFolder extends ReqUpdateFolder {

    public UpdateSeqItems updateItems;

    public ReqUpdateSeqFolder() {
        updateItems = new UpdateSeqItems();
    }

    static public class UpdateSeqItems {
        private int seq;

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        @Override
        public String toString() {
            return "UpdateItems{" +
                    ", seq=" + seq +
                    '}';
        }
    }

}
