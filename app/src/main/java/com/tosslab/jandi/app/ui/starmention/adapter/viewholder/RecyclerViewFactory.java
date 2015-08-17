package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

/**
 * Created by tee on 15. 7. 29..
 */
public class RecyclerViewFactory {

    public CommonStarMentionViewHolder getViewHolder(ViewGroup parent, int viewType) {
        if (viewType == StarMentionVO.Type.Text.getValue()) {
            return new MessageStarMentionViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_star_mention_message, parent, false));
        } else if (viewType == StarMentionVO.Type.Comment.getValue()) {
            return new CommentStarMentionViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_star_mention_comment, parent, false));
        } else if (viewType == StarMentionVO.Type.File.getValue()) {
            return new FileStarMentionViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_star_mention_file, parent, false));
        }
        return null;
    }
}