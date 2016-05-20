package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder;

import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;

/**
 * Created by tee on 16. 4. 16..
 */
public abstract class BaseViewHolderBuilder {

    protected boolean hasOnlyBadge = false;
    protected boolean hasProfile = false;
    protected boolean hasBottomMargin = false;
    protected boolean hasFileInfoView = false;
    protected boolean hasCommentBubbleTail = false;
    protected boolean hasNestedProfile = false;
    protected boolean hasViewAllComment = false;
    protected boolean hasSemiDivider = false;
    protected boolean hasFlatTop = false;
    protected boolean hasTopMargin = false;

    public BaseViewHolderBuilder setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
        return this;
    }

    public BaseViewHolderBuilder setHasUserProfile(boolean hasProfile) {
        this.hasProfile = hasProfile;
        return this;
    }

    public BaseViewHolderBuilder setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
        return this;
    }

    public BaseViewHolderBuilder setHasSemiDivider(boolean hasSemiDivider) {
        this.hasSemiDivider = hasSemiDivider;
        return this;
    }

    public BaseViewHolderBuilder setHasFileInfoView(boolean hasFileInfoView) {
        this.hasFileInfoView = hasFileInfoView;
        return this;
    }

    public BaseViewHolderBuilder setHasCommentBubbleTail(boolean hasCommentBubbleTail) {
        this.hasCommentBubbleTail = hasCommentBubbleTail;
        return this;
    }

    public BaseViewHolderBuilder setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
        return this;
    }

    public BaseViewHolderBuilder setHasViewAllComment(boolean hasViewAllComment) {
        this.hasViewAllComment = hasViewAllComment;
        return this;
    }

    public BaseViewHolderBuilder setHasFlatTop(boolean hasFlatTop) {
        this.hasFlatTop = hasFlatTop;
        return this;
    }

    public BaseViewHolderBuilder setHasTopMargin(boolean hasTopMargin) {
        this.hasTopMargin = hasTopMargin;
        return this;
    }

    public abstract BodyViewHolder build();


}
