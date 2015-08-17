package com.tosslab.jandi.app.ui.selector.room;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;

@EBean
public class RoomSelectorImpl implements RoomSelector {


    private OnRoomSelectListener onRoomSelectListener;
    private PopupWindow popupWindow;

    @Override
    public void show(View roomView) {
        Context context = roomView.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_room_selector, null);

        popupWindow = new PopupWindow(rootView);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_room_selector);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        RoomRecyclerAdapter adapter = new RoomRecyclerAdapter(context);
        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            if (onRoomSelectListener != null) {
                FormattedEntity item = adapter.getItem(position);
                onRoomSelectListener.onRoomSelect(item);
            }
        });

        recyclerView.setAdapter(adapter);

        View topicView = rootView.findViewById(R.id.tv_room_selector_topic);
        View dmView = rootView.findViewById(R.id.tv_room_selector_direct_message);

        View[] selectableViews = {topicView, dmView};

        topicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectType(0, selectableViews);
                getTopics().subscribe(adapter::addAll);
                adapter.notifyDataSetChanged();
                recyclerView.getLayoutManager().scrollToPosition(0);
            }
        });

        dmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectType(1, selectableViews);
                getUsers().subscribe(adapter::addAll);
                adapter.notifyDataSetChanged();
                recyclerView.getLayoutManager().scrollToPosition(0);
            }
        });

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        topicView.performClick();

        PopupWindowCompat.showAsDropDown(popupWindow, roomView, 0, 0, Gravity.TOP | Gravity.START);


    }

    private void setSelectType(int type, View[] selectableViews) {

        for (int idx = 0, size = selectableViews.length; idx < size; idx++) {
            if (idx == type) {
                selectableViews[idx].setSelected(true);
            } else {
                selectableViews[idx].setSelected(false);
            }
        }

    }

    protected Observable<List<FormattedEntity>> getUsers() {
        return Observable.merge(
                Observable.from(EntityManager.getInstance(JandiApplication.getContext()).getFormattedUsersWithoutMe()),
                Observable.from(Arrays.asList(new FormattedEntity(FormattedEntity.TYPE_EVERYWHERE))))
                .toSortedList((lhs, rhs) -> {
                    if (lhs.type == FormattedEntity.TYPE_EVERYWHERE) {
                        return -1;
                    } else if (rhs.type == FormattedEntity.TYPE_EVERYWHERE) {
                        return 1;
                    } else {
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    }
                });
    }

    protected Observable<List<FormattedEntity>> getTopics() {
        return Observable.merge(
                Observable.from(EntityManager.getInstance(JandiApplication.getContext()).getJoinedChannels()),
                Observable.from(EntityManager.getInstance(JandiApplication.getContext())
                        .getGroups()),
                Observable.from(Arrays.asList(new FormattedEntity(FormattedEntity.TYPE_EVERYWHERE))))
                .toSortedList((lhs, rhs) -> {
                    if (lhs.type == FormattedEntity.TYPE_EVERYWHERE) {
                        return -1;
                    } else if (rhs.type == FormattedEntity.TYPE_EVERYWHERE) {
                        return 1;
                    } else {
                        return lhs.getName().compareToIgnoreCase(rhs.getName());
                    }
                });
    }

    @Override
    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public void setOnRoomSelectListener(OnRoomSelectListener onRoomSelectListener) {
        this.onRoomSelectListener = onRoomSelectListener;
    }

    private static class RoomRecyclerAdapter extends RecyclerView.Adapter<RoomViewHolder> {

        private final Context context;
        private List<FormattedEntity> entities;
        private OnRecyclerItemClickListener onRecyclerItemClickListener;

        public RoomRecyclerAdapter(Context context) {
            this.context = context;
            entities = new ArrayList<>();
        }

        @Override
        public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select, parent, false);

            RoomViewHolder viewHolder = new RoomViewHolder(itemView);

            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            viewHolder.ivIcon = (ImageView) itemView.findViewById(R.id.iv_room_selector_item_icon);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RoomViewHolder holder, int position) {

            FormattedEntity item = getItem(position);

            holder.tvName.setText(item.getName());
            if (item.isUser()) {

                Glide.with(JandiApplication.getContext())
                        .load(BitmapUtil.getFileUrl(item.getUserSmallProfileUrl()))
                        .fitCenter()
                        .crossFade()
                        .transform(new GlideCircleTransform(holder.ivIcon.getContext()))
                        .into(holder.ivIcon);

                holder.ivIcon.setImageResource(R.drawable.jandi_icon_user);
            } else if (item.isPublicTopic()) {
                holder.ivIcon.setImageResource(R.drawable.icon_topic);
            } else {
                holder.ivIcon.setImageResource(R.drawable.icon_topic_private);
            }

            holder.itemView.setOnClickListener(v -> {
                if (onRecyclerItemClickListener != null) {
                    onRecyclerItemClickListener
                            .onItemClick(v, RoomRecyclerAdapter.this, position);
                }
            });
        }

        private FormattedEntity getItem(int position) {
            return entities.get(position);
        }

        @Override
        public int getItemCount() {
            return entities.size();
        }

        public void addAll(List<FormattedEntity> categorizableEntities) {
            entities.clear();
            entities.addAll(categorizableEntities);
        }

        public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
            this.onRecyclerItemClickListener = onRecyclerItemClickListener;
        }
    }

    private static class RoomViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private ImageView ivIcon;

        public RoomViewHolder(View itemView) {
            super(itemView);
        }
    }
}
