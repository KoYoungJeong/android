package com.tosslab.jandi.app.ui.share.multi.model;

import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;

public interface ShareAdapterDataModel {

    int size();

    ShareData getShareData(int position);

    void clear();

    void add(ShareData shareData);
}
