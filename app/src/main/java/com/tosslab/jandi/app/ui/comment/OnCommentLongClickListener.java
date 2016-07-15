package com.tosslab.jandi.app.ui.comment;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tonyjs on 16. 7. 12..
 */
public interface OnCommentLongClickListener {
    boolean onCommentLongClick(ResMessages.OriginalMessage comment);
}
