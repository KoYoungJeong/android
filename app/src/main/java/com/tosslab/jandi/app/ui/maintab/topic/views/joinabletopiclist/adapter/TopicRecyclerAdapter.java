package com.tosslab.jandi.app.ui.maintab.topic.views.joinabletopiclist.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemLongClickListener;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 7..
 */
public class TopicRecyclerAdapter extends RecyclerView.Adapter<TopicRecyclerAdapter.TopicViewHolder> {

    private final Context context;
    private List<Topic> topicList;

    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private OnRecyclerItemLongClickListener onRecyclerItemLongClickListener;
    private int selectedEntity;
    private AnimStatus animStatus = AnimStatus.READY;

    public TopicRecyclerAdapter(Context context) {
        this.context = context;
        topicList = new ArrayList<Topic>();
    }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_topic_body, parent, false);

        TopicViewHolder viewHolder = new TopicViewHolder(view);
        viewHolder.selector = view.findViewById(R.id.v_entity_listitem_selector);
        viewHolder.tvName = (TextView) view.findViewById(R.id.tv_entity_listitem_name);
        viewHolder.tvMemberCount = (TextView) view.findViewById(R.id.tv_entity_listitem_user_count);
        viewHolder.tvDescription =
                (TextView) view.findViewById(R.id.tv_entity_listitem_description);
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {
        Topic topic = getItem(position);
        if (topic == null) {
            return;
        }

        holder.draw(topic);

        if (topic.getEntityId() == selectedEntity && animStatus == AnimStatus.READY) {

            animStatus = AnimStatus.IN_ANIM;
            Integer colorFrom = context.getResources().getColor(R.color.transparent);
            Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_50);
            final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.setRepeatCount(1);
            colorAnimation.addUpdateListener(animator -> holder.selector.setBackgroundColor((Integer)
                    animator.getAnimatedValue()));

            colorAnimation.addListener(new SimpleEndAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animStatus = AnimStatus.FINISH;
                }
            });
            colorAnimation.start();
        }


        holder.itemView.setOnClickListener(view -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(holder.itemView, TopicRecyclerAdapter.this,
                        position);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (onRecyclerItemLongClickListener != null) {
                return onRecyclerItemLongClickListener.onItemClick(holder.itemView,
                        TopicRecyclerAdapter.this, position);
            }

            return false;
        });

    }

    public Topic getItem(int position) {
        if (getItemCount() <= position) {
            return null;
        }
        return topicList.get(position);
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public void addAll(List<Topic> topics) {
        topicList.addAll(topics);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public List<Topic> getTopics() {
        return Collections.unmodifiableList(topicList);
    }

    public void setOnRecyclerItemLongClickListener(OnRecyclerItemLongClickListener onRecyclerItemLongClickListener) {
        this.onRecyclerItemLongClickListener = onRecyclerItemLongClickListener;
    }


    public void clear() {
        topicList.clear();
    }

    public void setSelectedEntity(int selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    private enum AnimStatus {
        READY, IN_ANIM, FINISH, IDLE
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDescription;
        public View selector;
        private TextView tvName;
        private TextView tvMemberCount;

        public TopicViewHolder(View itemView) {
            super(itemView);
        }

        public void draw(Topic topic) {

            // 이름
            tvName.setText(topic.getName());
            // 추가 정보
            tvMemberCount.setText(String.format("(%d)", topic.getMemberCount()));

            String description = topic.getDescription();
            if (!TextUtils.isEmpty(description)) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

        }

    }

}
