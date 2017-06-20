package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.model.JoinableTopicDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.joinabletopiclist.view.JoinableTopicDataView;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 7..
 */
public class JoinableTopicListAdapter extends RecyclerView.Adapter<JoinableTopicListAdapter.TopicViewHolder>
        implements JoinableTopicDataModel, JoinableTopicDataView {

    private List<Topic> topicList;

    private OnRecyclerItemClickListener onTopicClickListener;

    public JoinableTopicListAdapter() {
        topicList = new ArrayList<>();
    }

    @Override
    public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic_body, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(TopicViewHolder holder, int position) {

        holder.itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measuredHeight = holder.itemView.getMeasuredHeight();

        Topic topic = getItem(position);
        if (topic == null) {
            return;
        }

        holder.draw(topic);

        holder.itemView.setOnClickListener(view -> {
            if (onTopicClickListener != null) {
                onTopicClickListener.onItemClick(
                        holder.itemView, JoinableTopicListAdapter.this, position);
            }
        });

        holder.itemView.getLayoutParams().height = measuredHeight;
        holder.itemView.setLayoutParams(holder.itemView.getLayoutParams());

    }

    @Nullable
    @Override
    public Topic getItem(int position) {
        if (getItemCount() <= position) {
            return null;
        }
        return topicList.get(position);
    }

    @Override
    public Topic getItemByEntityId(long entityId) {
        Topic topic = Observable.from(topicList)
                .filter(topic1 -> topic1.getEntityId() == entityId)
                .toBlocking()
                .firstOrDefault(new Topic.Builder().entityId(-1).build());
        return topic;
    }

    @Override
    public int getPositionByTopicEntityId(long entityId) {
        for (int i = 0; i < topicList.size(); i++) {
            Topic topic = topicList.get(i);
            if (topic.getEntityId() == entityId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    @Override
    public void setOnTopicClickListener(OnRecyclerItemClickListener onTopicClickListener) {
        this.onTopicClickListener = onTopicClickListener;
    }

    @Override
    public void clear() {
        topicList.clear();
    }

    @Override
    public synchronized void setJoinableTopics(List<Topic> topics) {
        topicList.addAll(topics);
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_entity_listitem_description)
        TextView tvDescription;
        @Bind(R.id.tv_user_name)
        TextView tvName;
        @Bind(R.id.tv_user_department)
        TextView tvMemberCount;

        public TopicViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
