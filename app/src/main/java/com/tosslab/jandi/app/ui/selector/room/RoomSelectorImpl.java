package com.tosslab.jandi.app.ui.selector.room;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.ui.selector.room.adapter.RoomRecyclerAdapter;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;

public class RoomSelectorImpl implements RoomSelector {

    public static final int TYPE_POPUP = 0;
    public static final int TYPE_VIEW = 1;

    private OnRoomSelectListener onRoomSelectListener;
    private PopupWindow popupWindow;
    private OnRoomDismissListener onRoomDismissListener;
    private boolean isIncludeAllMember;
    private List<FormattedEntity> users;
    private List<FormattedEntity> topics;
    private int type = TYPE_POPUP;

    public RoomSelectorImpl(List<FormattedEntity> topics, List<FormattedEntity> users) {
        this.users = users;
        this.topics = topics;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void show(View roomView) {
        dismiss();

        Context context = roomView.getContext();
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_room_selector, null);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_room_selector);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        RoomRecyclerAdapter adapter = new RoomRecyclerAdapter(context, RoomRecyclerAdapter.FROM_ROOM_SELECTOR);

        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            ExpandRoomData roomData = adapter.getItem(position);

            if (roomData instanceof ExpandRoomData.DummyDisabledRoomData) {
                ExpandRoomData.DummyDisabledRoomData dummy = (ExpandRoomData.DummyDisabledRoomData) roomData;
                boolean expanded = !dummy.isExpanded();
                dummy.setExpanded(expanded);
                adapter.notifyDataSetChanged();

                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            } else if (onRoomSelectListener != null) {
                onRoomSelectListener.onRoomSelect(roomData);
            }

        });

        recyclerView.setAdapter(adapter);

        View topicView = rootView.findViewById(R.id.tv_room_selector_topic);
        View dmView = rootView.findViewById(R.id.tv_room_selector_direct_message);

        View[] selectableViews = {topicView, dmView};

        topicView.setOnClickListener(v -> {
            setSelectType(0, selectableViews);
            adapter.clear();
            adapter.addAll(getTopicDatas());
            adapter.notifyDataSetChanged();
            recyclerView.getLayoutManager().scrollToPosition(0);
        });

        dmView.setOnClickListener(v -> {
            setSelectType(1, selectableViews);
            adapter.clear();
            adapter.addAll(getRoomDatas());
            adapter.notifyDataSetChanged();
            recyclerView.getLayoutManager().scrollToPosition(0);
        });

        if (type == TYPE_POPUP) {
            this.isIncludeAllMember = true;
            popupWindow = new PopupWindow(rootView);
            popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOnDismissListener(() -> {
                if (onRoomDismissListener != null) {
                    onRoomDismissListener.onRoomDismiss();
                }
            });
            popupWindow.setAnimationStyle(R.style.PopupAnimation);
            PopupWindowCompat.showAsDropDown(popupWindow,
                    roomView, 0, 0, Gravity.TOP | Gravity.START);
        } else if (type == TYPE_VIEW) {
            if (roomView instanceof ViewGroup) {
                ViewGroup rootViewGroup = (ViewGroup) roomView;
                this.isIncludeAllMember = false;
                ViewGroup.LayoutParams params =
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                rootViewGroup.addView(rootView, params);
            } else {
                throw new IllegalArgumentException("viewgroup needed");
            }
        }

        topicView.performClick();

    }

    public List<FormattedEntity> getTopics() {
        if (topics != null) {
            return topics;
        } else {
            return new ArrayList<>();
        }
    }

    public List<FormattedEntity> getUsers() {
        if (users != null) {
            return users;
        } else {
            return new ArrayList<>();
        }
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
    public LinkedHashMap<Long, FormattedEntity> getJoinTopics(List<FormattedEntity> entities) {
        LinkedHashMap<Long, FormattedEntity> topicHashMap = new LinkedHashMap<>();
        Observable.from(entities)
                .toSortedList((lhs, rhs) -> {

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
        List<ExpandRoomData> topicDatas = new ArrayList<>();

        // 로컬에서 가져오기
        List<ResFolderItem> topicFolderItems = repository.getFolderItems();
        List<ResFolder> topicFolders = repository.getFolders();

        LinkedHashMap<Long, FormattedEntity> joinTopics = getJoinTopics(getTopics());

        // File Search에서 모든 대화방을 표시하는 더미 데이터가 필요
        if (isIncludeAllMember) {
            ExpandRoomData dummyData = new ExpandRoomData();
            dummyData.setType(FormattedEntity.TYPE_EVERYWHERE);
            topicDatas.add(dummyData);
        }

        LinkedHashMap<Long, List<ExpandRoomData>> topicDataMap = new LinkedHashMap<>();

        for (ResFolder topicFolder : topicFolders) {
            if (!topicDataMap.containsKey(topicFolder.id)) {
                topicDataMap.put(topicFolder.id, new ArrayList<>());
            }
        }

        Observable.from(topicFolderItems)
                .filter(item -> item.folderId > 0)
                .subscribe(item -> {
                    ExpandRoomData topicData = new ExpandRoomData();
                    FormattedEntity topic = joinTopics.get(item.roomId);
                    if (topic != null) {
                        joinTopics.remove(item.roomId);
                        topicData.setEntityId(item.roomId);
                        topicData.setIsUser(false);
                        topicData.setName(topic.getName());
                        topicData.setType(topic.type);
                        topicData.setIsFolder(false);
                        topicData.setIsPublicTopic(topic.isPublicTopic());
                        topicData.setIsStarred(topic.isStarred);
                        topicDataMap.get(item.folderId).add(topicData);
                    }
                });

        for (ResFolder folder : topicFolders) {
            Collections.sort(topicDataMap.get(folder.id), (lhs, rhs) -> {
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
            for (ExpandRoomData roomData : topicDataMap.get(folder.id)) {
                topicDatas.add(roomData);
            }
        }

        boolean FirstAmongNoFolderItem = true;

        for (long key : joinTopics.keySet()) {
            FormattedEntity entity = joinTopics.get(key);
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

    public List<ExpandRoomData> getRoomDatas() {
        List<ExpandRoomData> roomDatas = new ArrayList<>();

        if (isIncludeAllMember) {
            ExpandRoomData dummyData = new ExpandRoomData();
            dummyData.setType(FormattedEntity.TYPE_EVERYWHERE);
            roomDatas.add(dummyData);
        }

        List<FormattedEntity> users = getUsers();
        Observable<List<ExpandRoomData>> enabledUsers = Observable.from(users)
                .filter(FormattedEntity::isEnabled)
                .map(ExpandRoomData::newRoomData)
                .toSortedList(this::getCompareRooms);

        Observable<List<ExpandRoomData>> disabledUsers = Observable.from(users)
                .filter(entity -> !entity.isEnabled())
                .map(ExpandRoomData::newRoomData)
                .toSortedList(this::getCompareRooms);

        boolean hasDisabledUser = hasDisabledUser(users);

        Observable<List<ExpandRoomData>> roomObservable;
        if (hasDisabledUser) {
            ExpandRoomData.DummyDisabledRoomData dummyDisabledRoomData =
                    new ExpandRoomData.DummyDisabledRoomData(getDisabledUserCount(users));

            String disabledMember = JandiApplication.getContext().getString(R.string.jandi_disabled_members);
            dummyDisabledRoomData.setName(disabledMember);

            roomObservable = Observable.concat(enabledUsers,
                    Observable.just(Arrays.asList(dummyDisabledRoomData)),
                    disabledUsers);
        } else {
            roomObservable = enabledUsers;
        }

        roomObservable.collect(() -> roomDatas, List::addAll)
                .subscribe();

        return roomDatas;
    }

    @NonNull
    private Integer getCompareRooms(ExpandRoomData lhs, ExpandRoomData rhs) {
        if (EntityManager.getInstance().isBot(lhs.getEntityId())) {
            return -1;
        } else if (EntityManager.getInstance().isBot(rhs.getEntityId())) {
            return 1;
        }
        return lhs.getName().compareToIgnoreCase(rhs.getName());
    }

    private int getDisabledUserCount(List<FormattedEntity> users) {
        return Observable.from(users)
                .filter(formattedEntity -> !formattedEntity.isEnabled())
                .count()
                .toBlocking()
                .first();
    }

    private boolean hasDisabledUser(List<FormattedEntity> users) {
        return Observable.from(users)
                .filter(formattedEntity -> !formattedEntity.isEnabled())
                .map(formattedEntity1 -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

}