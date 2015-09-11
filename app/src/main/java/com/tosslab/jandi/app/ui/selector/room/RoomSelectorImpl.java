package com.tosslab.jandi.app.ui.selector.room;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        RoomRecyclerAdapter adapter = new RoomRecyclerAdapter(context);
        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            if (onRoomSelectListener != null) {
                ExpandRoomData roomData = adapter.getItem(position);

                onRoomSelectListener.onRoomSelect(roomData);
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

                adapter.addAll(getTopicDatas());

                adapter.notifyDataSetChanged();
                recyclerView.getLayoutManager().scrollToPosition(0);
            }
        });

        dmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectType(1, selectableViews);

                List<ExpandRoomData> topicDatas = new ArrayList<>();

                getUsers().subscribe(entities -> {
                    for (FormattedEntity entity : entities) {
                        ExpandRoomData userData = new ExpandRoomData();

                        userData.setIsUser(true);
                        userData.setName(entity.getName());
                        try {
                            userData.setProfileUrl(entity.getUserSmallProfileUrl());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        userData.setType(entity.type);
                        if (entity.type != FormattedEntity.TYPE_EVERYWHERE) {
                            userData.setEntityId(entity.getId());
                        }
                        userData.setIsStarred(entity.isStarred);
                        userData.setIsFolder(false);
                        topicDatas.add(userData);
                    }
                });


                adapter.addAll(topicDatas);
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
                Observable.from(EntityManager.getInstance().getFormattedUsersWithoutMe()),
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
                Observable.from(EntityManager.getInstance().getJoinedChannels()),
                Observable.from(EntityManager.getInstance()
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

    // Join된 Topic에 관한 정보를 가져오기
    public LinkedHashMap<Integer, FormattedEntity> getJoinEntities() {

        EntityManager entityManager = EntityManager.getInstance();

        List<FormattedEntity> joinedChannels = entityManager.getJoinedChannels();
        List<FormattedEntity> groups = entityManager.getGroups();
        LinkedHashMap topicHashMap = new LinkedHashMap<Integer, Topic>();

        Observable<FormattedEntity> observable = Observable.merge(Observable.from(joinedChannels), Observable.from(groups));


        observable.toSortedList((lhs, rhs) -> {

            if (lhs.isStarred && rhs.isStarred) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            } else if (lhs.isStarred) {
                return -1;
            } else if (rhs.isStarred) {
                return 1;
            } else {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }

        }).subscribe(topics -> {
            for (FormattedEntity topic : topics) {
                topicHashMap.put(topic.getId(), topic);
            }
        });

        return topicHashMap;
    }

    public List<ExpandRoomData> getTopicDatas() {

        TopicFolderRepository repository = TopicFolderRepository.getRepository();

        List<ResFolder> topicFolders = null;
        List<ResFolderItem> topicFolderItems = null;
        List<ExpandRoomData> topicDatas = new ArrayList<>();

        // 로컬에서 가져오기
        topicFolderItems = repository.getFolderItems();
        topicFolders = repository.getFolders();

        LinkedHashMap<Integer, FormattedEntity> joinTopics = getJoinEntities();

        ExpandRoomData dummyData = new ExpandRoomData();
        dummyData.setType(FormattedEntity.TYPE_EVERYWHERE);
        topicDatas.add(dummyData);

        LinkedHashMap<Integer, List<ExpandRoomData>> topicDataMap = new LinkedHashMap<>();

        for (ResFolder topicFolder : topicFolders) {
            if (!topicDataMap.containsKey(topicFolder.id)) {
                topicDataMap.put(new Integer(topicFolder.id), new ArrayList<>());
            }
        }

        Observable.from(topicFolderItems)
                .filter(item -> item.folderId > 0)
                .subscribe(item -> {
                    ExpandRoomData topicData = new ExpandRoomData();
                    FormattedEntity topic = joinTopics.get(item.roomId);
                    joinTopics.remove(item.roomId);
                    topicData.setEntityId(item.roomId);
                    topicData.setIsUser(false);
                    topicData.setName(topic.getName());
                    topicData.setType(topic.type);
                    topicData.setIsFolder(false);
                    topicData.setIsPublicTopic(topic.isPublicTopic());
                    topicData.setIsStarred(topic.isStarred);
                    topicDataMap.get(new Integer(item.folderId)).add(topicData);
                });

        for (ResFolder folder : topicFolders) {
            Collections.sort(topicDataMap.get(new Integer(folder.id)), (lhs, rhs) -> {
                if (lhs.isStarred() && rhs.isStarred()) {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                } else if (lhs.isStarred()) {
                    return -1;
                } else if (rhs.isStarred()) {
                    return 1;
                } else {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                }
            });
        }

        // 각 폴더와 종속된 토픽 데이터 셋팅
        for (ResFolder folder : topicFolders) {
            ExpandRoomData folderdata = new ExpandRoomData();
            folderdata.setIsFolder(true);
            folderdata.setIsUser(false);
            folderdata.setName(folder.name);
            topicDatas.add(folderdata);
            for (ExpandRoomData roomData : topicDataMap.get(new Integer(folder.id))) {
                topicDatas.add(roomData);
            }
        }

        Iterator joinTopicKeySets = joinTopics.keySet().iterator();

        boolean FirstAmongNoFolderItem = true;

        while (joinTopicKeySets.hasNext()) {
            FormattedEntity entity = joinTopics.get(joinTopicKeySets.next());
            ExpandRoomData topicData = new ExpandRoomData();
            topicData.setIsFirstAmongNoFolderItem(FirstAmongNoFolderItem);
            FirstAmongNoFolderItem = false;
            topicData.setEntityId(entity.getId());
            topicData.setIsUser(false);
            topicData.setName(entity.getName());
            topicData.setType(entity.type);
            topicData.setIsFolder(false);
            topicData.setIsPublicTopic(entity.isPublicTopic());
            topicData.setIsStarred(entity.isStarred);

            topicDatas.add(topicData);
        }

        return topicDatas;
    }

    private static class RoomRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final int TYPE_FOLDER = 1;
        final int TYPE_ROOM = 2;

        private final Context context;
        private List<ExpandRoomData> roomDatas;
        private OnRecyclerItemClickListener onRecyclerItemClickListener;

        public RoomRecyclerAdapter(Context context) {
            this.context = context;
            roomDatas = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            if (viewType == TYPE_ROOM) {

                itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_room_select, parent, false);

                RoomViewHolder viewHolder = new RoomViewHolder(itemView);

                viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);
                viewHolder.ivIcon = (ImageView) itemView.findViewById(R.id.iv_room_selector_item_icon);
                viewHolder.vgLine = (LinearLayout) itemView.findViewById(R.id.ll_line_use_for_first_no_folder_item);

                return viewHolder;

            } else if (viewType == TYPE_FOLDER) {

                itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_room_select_folder, parent, false);
                FolderViewHolder viewHolder = new FolderViewHolder(itemView);
                viewHolder.tvName = (TextView) itemView.findViewById(R.id.tv_room_selector_item_name);

                return viewHolder;
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ExpandRoomData item = getItem(position);

            if (getItemViewType(position) == TYPE_FOLDER) {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                folderViewHolder.tvName.setText(item.getName().toString());
                folderViewHolder.itemView.setClickable(false);
                return;
            }

            RoomViewHolder roomholder = (RoomViewHolder) holder;

            // 폴더가 없는 첫번째 폴더는 상단에 라인이 그려져야 함.
            if (item.isFirstAmongNoFolderItem()) {
                roomholder.vgLine.setVisibility(View.VISIBLE);
            } else {
                roomholder.vgLine.setVisibility(View.GONE);
            }

            if (item.getType() == FormattedEntity.TYPE_EVERYWHERE) {
                roomholder.ivIcon.setImageResource(R.drawable.icon_search_all);
                roomholder.tvName.setText(R.string.jandi_file_category_everywhere);
            } else if (item.isUser()) {
                Ion.with(roomholder.ivIcon)
                        .placeholder(R.drawable.profile_img_comment)
                        .error(R.drawable.profile_img_comment)
                        .fitCenter()
                        .transform(new IonCircleTransform())
                        .crossfade(true)
                        .load(BitmapUtil.getFileUrl(item.getProfileUrl()));

                roomholder.tvName.setText(item.getName());
            } else if (item.isPublicTopic()) {
                if (item.isStarred) {
                    roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
                } else {
                    roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic);
                }
                roomholder.tvName.setText(item.getName());
            } else {
                if (item.isStarred) {
                    roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
                } else {
                    roomholder.ivIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
                }
                roomholder.tvName.setText(item.getName());
            }

            holder.itemView.setOnClickListener(v -> {
                if (onRecyclerItemClickListener != null) {
                    onRecyclerItemClickListener
                            .onItemClick(v, RoomRecyclerAdapter.this, position);
                }
            });
        }

        private ExpandRoomData getItem(int position) {
            return roomDatas.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position).isFolder()) {
                return TYPE_FOLDER;
            } else {
                return TYPE_ROOM;
            }
        }

        @Override
        public int getItemCount() {
            return roomDatas.size();
        }

        public void addAll(List<ExpandRoomData> categorizableEntities) {
            roomDatas.clear();
            roomDatas.addAll(categorizableEntities);
        }

        public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
            this.onRecyclerItemClickListener = onRecyclerItemClickListener;
        }
    }

    private static class RoomViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private ImageView ivIcon;
        private LinearLayout vgLine;

        public RoomViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class FolderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;

        public FolderViewHolder(View itemView) {
            super(itemView);
        }

    }

    public static class ExpandRoomData {
        private int entityId;
        private String name;
        private boolean isFolder;
        private boolean isUser;
        private boolean isStarred;
        private boolean isPublicTopic;
        private String profileUrl;
        private int type;
        private boolean isFirstAmongNoFolderItem;

        public int getEntityId() {
            return entityId;
        }

        public void setEntityId(int entityId) {
            this.entityId = entityId;
        }

        public boolean isPublicTopic() {
            return isPublicTopic;
        }

        public void setIsPublicTopic(boolean isPublicTopic) {
            this.isPublicTopic = isPublicTopic;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public boolean isUser() {
            return isUser;
        }

        public void setIsUser(boolean isUser) {
            this.isUser = isUser;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isFolder() {
            return isFolder;
        }

        public void setIsFolder(boolean isFolder) {
            this.isFolder = isFolder;
        }

        public boolean isStarred() {
            return isStarred;
        }

        public void setIsStarred(boolean isStarred) {
            this.isStarred = isStarred;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(@Nullable String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public boolean isFirstAmongNoFolderItem() {
            return isFirstAmongNoFolderItem;
        }

        public void setIsFirstAmongNoFolderItem(boolean isFirstAmongNoFolderItem) {
            this.isFirstAmongNoFolderItem = isFirstAmongNoFolderItem;
        }
    }
}
