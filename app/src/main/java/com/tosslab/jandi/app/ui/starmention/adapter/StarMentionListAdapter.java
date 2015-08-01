package com.tosslab.jandi.app.ui.starmention.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.starmention.viewholder.CommentViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.FileViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.MessageViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.RecyclerViewFactory;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarMentionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<StarMentionVO> starMentionList = new ArrayList<StarMentionVO>();
    private RecyclerViewFactory recyclerViewFactory;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (recyclerViewFactory == null) {
            recyclerViewFactory = new RecyclerViewFactory();
        }
        return recyclerViewFactory.getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StarMentionVO starMentionVO = starMentionList.get(position);

        if (getItemViewType(position) == StarMentionVO.Type.Text.getValue()) {

            MessageViewHolder viewHolder = (MessageViewHolder) holder;
            Ion.with(viewHolder.getStarMentionProfileView())
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new IonCircleTransform())
                    .crossfade(true)
                    .load(starMentionVO.getWriterPictureUrl());
            viewHolder.getStarMentionNameView().setText(starMentionVO.getWriterName());
            viewHolder.getStarMentionContentView().setText(starMentionVO.getContent());
            viewHolder.getStarMentionTopicNameView().setText(starMentionVO.getRoomName());
            String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
            viewHolder.getStarMentionDateView().setText(updateTime);

        } else if (getItemViewType(position) == StarMentionVO.Type.Comment.getValue()) {

            CommentViewHolder viewHolder = (CommentViewHolder) holder;
            Ion.with(viewHolder.getStarMentionProfileView())
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new IonCircleTransform())
                    .crossfade(true)
                    .load(starMentionVO.getWriterPictureUrl());
            viewHolder.getStarMentionNameView().setText(starMentionVO.getWriterName());
            viewHolder.getStarMentionCommentView().setText(starMentionVO.getContent());
            viewHolder.getStarMentionFileNameView().setText(starMentionVO.getFileName());
            String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
            viewHolder.getStarMentionDateView().setText(updateTime);

        } else if (getItemViewType(position) == StarMentionVO.Type.File.getValue()) {

            FileViewHolder viewHolder = (FileViewHolder) holder;
            Ion.with(viewHolder.getStarMentionProfileView())
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new IonCircleTransform())
                    .crossfade(true)
                    .load(starMentionVO.getWriterPictureUrl());
            viewHolder.getStarMentionNameView().setText(starMentionVO.getWriterName());
            viewHolder.getStarMentionFileNameView().setText(starMentionVO.getFileName());
            String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());
            viewHolder.getStarMentionDateView().setText(updateTime);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return starMentionList.get(position).getContentType();
    }

    @Override
    public int getItemCount() {
        return starMentionList.size();
    }

    public void setStarMentionList(List<StarMentionVO> starMentionList) {
        this.starMentionList = starMentionList;
    }

}
