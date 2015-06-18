package com.tosslab.jandi.app.lists.files.viewholder;

import android.view.View;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public interface CommentViewHolder {

    void init(View rootView);
    void bind(ResMessages.OriginalMessage originalMessage);
    int getLayoutResourceId();
}
