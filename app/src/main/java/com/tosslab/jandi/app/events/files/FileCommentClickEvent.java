package com.tosslab.jandi.app.events.files;

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

    public FileCommentClickEvent(ResMessages.OriginalMessage comment, boolean isLongClick) {
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
