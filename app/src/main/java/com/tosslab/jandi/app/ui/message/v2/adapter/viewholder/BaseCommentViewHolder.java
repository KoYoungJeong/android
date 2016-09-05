package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tee on 16. 4. 7..
 */
public abstract class BaseCommentViewHolder implements BodyViewHolder {

    protected ViewStub stubContentInfo;
    protected ViewGroup vgContentInfo;
    protected View vCommentSemiDivider;
    protected View vCommentNormalDivider;
    protected View vMargin;
    protected ViewGroup vgMessageLastRead;
    protected View vCommentBubbleTail;
    protected ViewStub stubReadMore;
    protected ViewGroup vgReadMore;

    protected boolean hasBottomMargin = false;
    protected boolean hasSemiDivider = false;
    private boolean hasCommentBubbleTail = false;
    private boolean hasViewAllComment = false;
    private boolean hasContentInfo = false;
    private TextView tvReadMore;


    @Override
    public void initView(View rootView) {
        stubContentInfo = (ViewStub) rootView.findViewById(R.id.vg_content);
        stubReadMore = (ViewStub) rootView.findViewById(R.id.vg_read_more);

        vgMessageLastRead = (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        vCommentSemiDivider = rootView.findViewById(R.id.v_comment_semi_divider);
        vCommentNormalDivider = rootView.findViewById(R.id.v_comment_normal_divider);
        vMargin = rootView.findViewById(R.id.v_margin);
        vCommentBubbleTail = rootView.findViewById(R.id.iv_comment_bubble_tail);

        initObjects();
    }

    protected void setOptionView() {
        if (hasContentInfo && stubContentInfo != null) {
            vgContentInfo = (ViewGroup) stubContentInfo.inflate();
        }

        if (hasViewAllComment) {
            if (stubReadMore != null) {
                vgReadMore = (ViewGroup) stubReadMore.inflate();
            }
            tvReadMore = (TextView) vgReadMore.findViewById(R.id.tv_comment_read_more);
        }

        if (vCommentBubbleTail != null) {
            if (hasCommentBubbleTail) {
                vCommentBubbleTail.setVisibility(View.VISIBLE);
            } else {
                vCommentBubbleTail.setVisibility(View.GONE);
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
        } else {
            vCommentSemiDivider.setVisibility(View.GONE);
            vCommentNormalDivider.setVisibility(View.VISIBLE);
            vMargin.setVisibility(View.GONE);
        }
    }

    abstract protected void initObjects();

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        if (hasViewAllComment) {
            int count = link.feedback instanceof ResMessages.Commentable
                    ? ((ResMessages.Commentable) link.feedback).getCommentCount() : 0;
            String countString
                    = JandiApplication.getContext().getResources().getString(R.string.jandi_view_comment_read_more, count);
            tvReadMore.setText(countString);
        }
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (vgMessageLastRead != null) {
            if (currentLinkId == lastReadLinkId) {
                vgMessageLastRead.removeAllViews();
                LayoutInflater.from(vgMessageLastRead.getContext()).inflate(R.layout.item_message_last_read_v2, vgMessageLastRead);
                vgMessageLastRead.setVisibility(View.VISIBLE);
            } else {
                vgMessageLastRead.setVisibility(View.GONE);
            }
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

    public boolean hasCommentBubbleTail() {
        return hasCommentBubbleTail;
    }

    protected void setHasViewAllComment(boolean hasViewAllComment) {
        this.hasViewAllComment = hasViewAllComment;
    }

    protected void setHasContentInfo(boolean hasContentInfo) {
        this.hasContentInfo = hasContentInfo;
    }

    protected boolean hasContentInfo() {
        return hasContentInfo;
    }
}
