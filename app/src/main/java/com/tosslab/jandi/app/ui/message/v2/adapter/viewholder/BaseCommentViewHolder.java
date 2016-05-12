package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tee on 16. 4. 7..
 */
public abstract class BaseCommentViewHolder implements BodyViewHolder {

    protected ViewStub stubFileInfo;
    protected ViewGroup vgFileInfo;
    protected View vCommentSemiDivider;
    protected View vCommentNormalDivider;
    protected View vMargin;
    protected ViewGroup vgMessageLastRead;
    protected ImageView ivCommentBubbleTail;
    protected ViewStub stubReadMore;
    protected ViewGroup vgReadMore;

    protected boolean hasBottomMargin = false;
    protected boolean hasSemiDivider = false;
    private boolean hasCommentBubbleTail = false;
    private boolean hasViewAllComment = false;
    private boolean hasFileInfoView = false;
    private TextView tvReadMore;


    @Override
    public void initView(View rootView) {
        stubFileInfo = (ViewStub) rootView.findViewById(R.id.vg_file_item);
        stubReadMore = (ViewStub) rootView.findViewById(R.id.vg_read_more);

        vgMessageLastRead = (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        vCommentSemiDivider = rootView.findViewById(R.id.v_comment_semi_divider);
        vCommentNormalDivider = rootView.findViewById(R.id.v_comment_normal_divider);
        vMargin = rootView.findViewById(R.id.v_margin);
        ivCommentBubbleTail = (ImageView) rootView.findViewById(R.id.iv_comment_bubble_tail);

        initObjects();
        setOptionView();
    }

    private void setOptionView() {
        if (hasFileInfoView && stubFileInfo != null) {
            vgFileInfo = (ViewGroup) stubFileInfo.inflate();
        }

        if (hasViewAllComment) {
            if (stubReadMore != null) {
                vgReadMore = (ViewGroup) stubReadMore.inflate();
            }
            tvReadMore = (TextView) vgReadMore.findViewById(R.id.tv_comment_read_more);
        }

        if (ivCommentBubbleTail != null) {
            if (hasCommentBubbleTail) {
                ivCommentBubbleTail.setVisibility(View.VISIBLE);
            } else {
                ivCommentBubbleTail.setVisibility(View.GONE);
            }
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
