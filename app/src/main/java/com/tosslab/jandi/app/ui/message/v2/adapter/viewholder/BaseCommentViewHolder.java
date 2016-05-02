package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tee on 16. 4. 7..
 */
public abstract class BaseCommentViewHolder implements BodyViewHolder {

    protected ViewGroup vgFileItem;
    protected ViewGroup vgProfileNestedComment;
    protected ViewGroup vgProfileNestedCommentSticker;
    protected View vCommentSemiDivider;
    protected View vCommentNormalDivider;
    protected View vMargin;
    protected ViewGroup vgMessageLastRead;
    protected ImageView ivCommentBubbleTail;
    protected ViewGroup vgReadMore;

    protected boolean hasBottomMargin = false;
    protected boolean hasSemiDivider = false;
    private boolean hasCommentBubbleTail = false;
    private boolean hasViewAllComment = false;
    private boolean hasFileInfoView = false;


    @Override
    public void initView(View rootView) {
        vgFileItem =
                (ViewGroup) rootView.findViewById(R.id.vg_file_item);
        vgReadMore = (ViewGroup) rootView.findViewById(R.id.vg_read_more);

        vgProfileNestedComment =
                (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment);

        vgProfileNestedCommentSticker =
                (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment_sticker);

        vgMessageLastRead =
                (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        vCommentSemiDivider = rootView.findViewById(R.id.v_comment_semi_divider);
        vCommentNormalDivider = rootView.findViewById(R.id.v_comment_normal_divider);
        vMargin = rootView.findViewById(R.id.v_margin);
        ivCommentBubbleTail = (ImageView) rootView.findViewById(R.id.iv_comment_bubble_tail);

        setOptionView();
        initObjects();
    }

    private void setOptionView() {
        if (hasFileInfoView) {
            vgFileItem.setVisibility(View.VISIBLE);
        } else {
            vgFileItem.setVisibility(View.GONE);
        }

        if (hasCommentBubbleTail) {
            ivCommentBubbleTail.setVisibility(View.VISIBLE);
        } else {
            ivCommentBubbleTail.setVisibility(View.GONE);
        }

        if (hasViewAllComment) {
            vgReadMore.setVisibility(View.VISIBLE);
        } else {
            vgReadMore.setVisibility(View.GONE);
        }

        if (hasSemiDivider) {
            vCommentSemiDivider.setVisibility(View.VISIBLE);
            vCommentNormalDivider.setVisibility(View.GONE);
            vMargin.setVisibility(View.GONE);
            return;
        }

        if (hasBottomMargin) {
            vCommentSemiDivider.setVisibility(View.GONE);
            vCommentNormalDivider.setVisibility(View.GONE);
            vMargin.setVisibility(View.VISIBLE);
            return;
        } else {
            vCommentSemiDivider.setVisibility(View.GONE);
            vCommentNormalDivider.setVisibility(View.VISIBLE);
            vMargin.setVisibility(View.GONE);
            return;
        }
    }

    abstract protected void initObjects();

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        if (hasViewAllComment) {
            int count = link.feedback.commentCount;
            String countString
                    = JandiApplication.getContext().getResources().getString(R.string.jandi_view_comment_read_more, count);
            TextView tvReadMore = (TextView) vgReadMore.findViewById(R.id.tv_comment_read_more);
            tvReadMore.setText(countString);
        }
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vgMessageLastRead.setVisibility(View.VISIBLE);
        } else {
            vgMessageLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_comment_v3;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {

    }

    protected void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    protected void setHasSemiDivider(boolean hasSemiDivider) {
        this.hasSemiDivider = hasSemiDivider;
    }

    protected void setHasCommentBubbleTail(boolean hasCommentBubbleTail) {
        this.hasCommentBubbleTail = hasCommentBubbleTail;
    }

    protected void setHasViewAllComment(boolean hasViewAllComment) {
        this.hasViewAllComment = hasViewAllComment;
    }

    protected void setHasFileInfoView(boolean hasFileInfoView) {
        this.hasFileInfoView = hasFileInfoView;
    }

    protected boolean hasFileInfoView() {
        return hasFileInfoView;
    }
}
