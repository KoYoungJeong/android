package com.tosslab.jandi.app.ui.starmention.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

/**
 * Created by tee on 15. 7. 29..
 */
public class RecyclerViewFactory {

    public RecyclerView.ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == StarMentionVO.Type.Text.getValue()) {
            return new MessageViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_star_mention_message, parent, false));
        } else if (viewType == StarMentionVO.Type.Comment.getValue()) {
            return new CommentViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_star_mention_comment, parent, false));
        } else if (viewType == StarMentionVO.Type.File.getValue()) {
            return new FileViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_star_mention_file, parent, false));
        }
        return null;
    }
}
