package com.tosslab.jandi.app.events.files;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tonyjs on 16. 2. 5..
 */
public class FileCommentClickEvent {
    private boolean isLongClick = false;
    private ResMessages.OriginalMessage comment;

    public FileCommentClickEvent(ResMessages.OriginalMessage comment) {
        this.comment = comment;
    }

    public FileCommentClickEvent(boolean isLongClick, ResMessages.OriginalMessage comment) {
        this.isLongClick = isLongClick;
        this.comment = comment;
    }

    public boolean isLongClick() {
        return isLongClick;
    }

    public ResMessages.OriginalMessage getComment() {
        return comment;
    }

    public void setComment(ResMessages.OriginalMessage comment) {
        this.comment = comment;
    }
}
