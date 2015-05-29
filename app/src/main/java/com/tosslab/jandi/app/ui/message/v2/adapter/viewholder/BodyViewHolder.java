package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public interface BodyViewHolder {

    void initView(View rootView);

    void bindData(ResMessages.Link link, int teamId, int roomId);

    int getLayoutId();

    enum Type {
        Message, PureMessage,
        File,
        Image,
        FileComment, PureComment, CollapseComment,
        Dummy, DummyPure,
        Event
    }
}
