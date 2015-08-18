package com.tosslab.jandi.app.ui.selector.room;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;

public class RoomSelectorImpl implements RoomSelector {


    private OnRoomSelectListener onRoomSelectListener;
    private PopupWindow popupWindow;
    private OnRoomDismissListener onRoomDismissListener;

    @Override
    public void show(View roomView) {

        dismiss();

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
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (onRoomDismissListener != null) {
                    onRoomDismissListener.onRoomDismiss();
                }
            }
        });

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
                .filter(formattedEntity -> formattedEntity.type == FormattedEntity.TYPE_EVERYWHERE
                        || TextUtils.equals(formattedEntity.getUser().status, "enabled"))
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

    @Override
    public void setOnRoomDismissListener(OnRoomDismissListener onRoomDismissListener) {
        this.onRoomDismissListener = onRoomDismissListener;
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

            if (item.type == FormattedEntity.TYPE_EVERYWHERE) {
                holder.ivIcon.setImageResource(R.drawable.icon_search_all);
                holder.tvName.setText(R.string.jandi_file_category_everywhere);
            } else if (item.isUser()) {
                Ion.with(holder.ivIcon)
                        .placeholder(R.drawable.jandi_profile_comment)
                        .error(R.drawable.jandi_profile_comment)
                        .fitCenter()
                        .transform(new IonCircleTransform())
                        .crossfade(true)
                        .load(BitmapUtil.getFileUrl(item.getUserSmallProfileUrl()));

                holder.tvName.setText(item.getName());
            } else if (item.isPublicTopic()) {
                if (item.isStarred) {
                    holder.ivIcon.setImageResource(R.drawable.icon_topic_f);
                } else {
                    holder.ivIcon.setImageResource(R.drawable.icon_topic);
                }
                holder.tvName.setText(item.getName());
            } else {
                if (item.isStarred) {
                    holder.ivIcon.setImageResource(R.drawable.icon_topic_private_f);
                } else {
                    holder.ivIcon.setImageResource(R.drawable.icon_topic_private);
                }
                holder.tvName.setText(item.getName());
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
