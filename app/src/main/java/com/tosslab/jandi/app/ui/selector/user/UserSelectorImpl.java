package com.tosslab.jandi.app.ui.selector.user;


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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class UserSelectorImpl implements UserSelector {

    private PopupWindow popupWindow;
    private OnUserSelectListener onRoomSelectListener;
    private OnUserDismissListener onUserDismissListener;

    @Override
    public void show(View roomView) {
        dismiss();

        Context context = roomView.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_user_selector, null);

        popupWindow = new PopupWindow(rootView);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_user_selector);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        UserRecyclerAdapter adapter = new UserRecyclerAdapter(context);
        getUsers().subscribe(adapter::addAll);

        if (EntityManager.getInstance().hasJandiBot()) {
            adapter.add(0, EntityManager.getInstance().getJandiBot());
        }

        adapter.add(0, new FormattedEntity(FormattedEntity.TYPE_EVERYWHERE));

        adapter.setOnRoomSelectListener(onRoomSelectListener);
        recyclerView.setAdapter(adapter);

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setOnDismissListener(() -> {
            if (onUserDismissListener != null) {
                onUserDismissListener.onUserDismiss();
            }
        });
        PopupWindowCompat.showAsDropDown(popupWindow, roomView, 0, 0, Gravity.TOP | Gravity.START);

    }

    protected Observable<List<FormattedEntity>> getUsers() {

        EntityManager entityManager = EntityManager.getInstance();
        int myId = entityManager.getMe().getId();
        return Observable.from(entityManager.getFormattedUsers())
                .filter(formattedEntity -> TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .toSortedList((lhs, rhs) -> {
                    if (lhs.getId() == myId) {
                        return -1;
                    } else if (rhs.getId() == myId) {
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
    public void setOnUserSelectListener(OnUserSelectListener onRoomSelectListener) {
        this.onRoomSelectListener = onRoomSelectListener;
    }

    @Override
    public void setOnUserDismissListener(OnUserDismissListener onUserDismissListener) {
        this.onUserDismissListener = onUserDismissListener;
    }

    private static class UserRecyclerAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private final Context context;
        private List<FormattedEntity> entities;
        private OnUserSelectListener onRoomSelectListener;

        public UserRecyclerAdapter(Context context) {
            this.context = context;
            entities = new ArrayList<>();
        }

        public void addAll(List<FormattedEntity> entities) {
            this.entities.clear();
            this.entities.addAll(entities);
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_room_select, parent, false);

            UserViewHolder viewHolder = new UserViewHolder(itemView);

            viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
            viewHolder.ivIcon =
                    (SimpleDraweeView) itemView.findViewById(R.id.iv_room_selector_item_icon);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            FormattedEntity item = getItem(position);

            SimpleDraweeView ivIcon = holder.ivIcon;
            if (item.type == FormattedEntity.TYPE_EVERYWHERE) {
                holder.tvName.setText(R.string.jandi_file_category_everyone);
                ImageLoader.newBuilder()
                        .placeHolder(R.drawable.icon_search_all_members, ScalingUtils.ScaleType.CENTER_INSIDE)
                        .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .load(UriFactory.getResourceUri(R.drawable.icon_search_all_members))
                        .into(ivIcon);
            } else {
                boolean user = !EntityManager.getInstance().isBot(item.getId());

                ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();
                if (user) {
                    layoutParams.height = layoutParams.width;
                } else {
                    layoutParams.height = layoutParams.width * 5 / 4;
                }

                if (user) {
                    ImageUtil.loadProfileImage(ivIcon,
                            item.getUserSmallProfileUrl(), R.drawable.profile_img_comment);
                } else {
                    ImageLoader.newBuilder()
                            .placeHolder(R.drawable.bot_32x40, ScalingUtils.ScaleType.CENTER_INSIDE)
                            .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                            .load(UriFactory.getResourceUri(R.drawable.bot_32x40))
                            .into(ivIcon);

                }
                holder.tvName.setText(item.getName());
            }

            holder.itemView.setOnClickListener(v -> {
                if (onRoomSelectListener != null) {
                    onRoomSelectListener.onUserSelect(item);
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

        public void setOnRoomSelectListener(OnUserSelectListener onRoomSelectListener) {
            this.onRoomSelectListener = onRoomSelectListener;
        }

        public void add(int position, FormattedEntity entity) {
            entities.add(position, entity);
        }
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public SimpleDraweeView ivIcon;

        public UserViewHolder(View itemView) {
            super(itemView);
        }
    }
}
