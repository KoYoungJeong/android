package com.tosslab.jandi.app.ui.share.multi.model;

import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;

import java.util.List;

public class SharesDataModel {

    private List<ShareData> shareDatas;

    public SharesDataModel(List<ShareData> shareDatas) {
        this.shareDatas = shareDatas;
    }

    public int size() {
        return shareDatas.size();
    }

    public ShareData getItem(int position) {
        return shareDatas.get(position);
    }

    synchronized public void clear() {
        shareDatas.clear();
    }

    synchronized public void add(ShareData shareData) {
        shareDatas.add(shareData);
    }
}
