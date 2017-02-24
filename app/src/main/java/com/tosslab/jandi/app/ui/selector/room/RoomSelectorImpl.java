package com.tosslab.jandi.app.ui.selector.room;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.selector.room.adapter.RoomRecyclerAdapter;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.StringCompareUtil;

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
    private List<Member> users;
    private List<TopicRoom> topics;
    private int type = TYPE_POPUP;

    public RoomSelectorImpl(List<TopicRoom> topics, List<Member> users) {
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
            if (SdkUtils.isOverNougat()) {
                int[] a = new int[2];
                roomView.getLocationInWindow(a);
                popupWindow.showAtLocation(((Activity) roomView.getContext()).getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, a[1] + roomView.getHeight());
            } else {
                PopupWindowCompat.showAsDropDown(popupWindow, roomView, 0, 0, Gravity.TOP | Gravity.LEFT);
            }
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

    public List<TopicRoom> getTopics() {
        if (topics != null) {
            return topics;
        } else {
            return new ArrayList<>();
        }
    }

    public List<Member> getUsers() {
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
    public LinkedHashMap<Long, TopicRoom> getJoinTopics(List<TopicRoom> entities) {
        LinkedHashMap<Long, TopicRoom> topicHashMap = new LinkedHashMap<>();
        Observable.from(entities)
                .toSortedList((lhs, rhs) -> {

                    if (lhs.isStarred() && rhs.isStarred()) {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    } else if (lhs.isStarred()) {
                        return -1;
                    } else if (rhs.isStarred()) {
                        return 1;
                    } else {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    }

                }).subscribe(topics -> {
            for (TopicRoom topic : topics) {
                topicHashMap.put(topic.getId(), topic);
            }
        }, Throwable::printStackTrace);

        return topicHashMap;
    }

    public List<ExpandRoomData> getTopicDatas() {
        List<ExpandRoomData> topicDatas = new ArrayList<>();

        List<TopicFolder> topicFolders = TeamInfoLoader.getInstance().getTopicFolders();

        LinkedHashMap<Long, TopicRoom> joinTopics = getJoinTopics(getTopics());

        // File Search에서 모든 대화방을 표시하는 더미 데이터가 필요
        if (isIncludeAllMember) {

            ExpandRoomData dummyData;
            if (TeamInfoLoader.getInstance().getMyLevel() != Level.Guest) {
                dummyData = new ExpandRoomData();
                dummyData.setType(JandiConstants.Entity.TYPE_EVERYWHERE);
                topicDatas.add(dummyData);
            }

            dummyData = new ExpandRoomData();
            dummyData.setType(JandiConstants.Entity.TYPE_JOINED_ROOM);
            topicDatas.add(dummyData);
        }

        LinkedHashMap<Long, List<ExpandRoomData>> topicDataMap = new LinkedHashMap<>();

        for (TopicFolder topicFolder : topicFolders) {
            if (!topicDataMap.containsKey(topicFolder.getId())) {
                topicDataMap.put(topicFolder.getId(), new ArrayList<>());
            }
        }

        Observable.from(topicFolders)
                .flatMap(topicFolder -> Observable.from(topicFolder.getRooms())
                        .map(topicRoom -> Pair.create(topicFolder, topicRoom))
                )
                .subscribe(pair -> {
                    ExpandRoomData topicData = new ExpandRoomData();
                    TopicRoom topic = joinTopics.get(pair.second.getId());
                    if (topic != null) {
                        joinTopics.remove(pair.second.getId());
                        topicData.setEntityId(pair.second.getId());
                        topicData.setIsUser(false);
                        topicData.setName(topic.getName());
                        topicData.setIsFolder(false);
                        topicData.setIsPublicTopic(topic.isPublicTopic());
                        topicData.setIsStarred(topic.isStarred());
                        topicDataMap.get(pair.first.getId()).add(topicData);
                    }
                });

        for (TopicFolder folder : topicFolders) {
            Collections.sort(topicDataMap.get(folder.getId()), (lhs, rhs) -> {
                if (lhs.isStarred() && rhs.isStarred()) {
                    return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                } else if (lhs.isStarred()) {
                    return -1;
                } else if (rhs.isStarred()) {
                    return 1;
                } else {
                    return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                }
            });
        }

        // 각 폴더와 종속된 토픽 데이터 셋팅
        for (TopicFolder folder : topicFolders) {
            ExpandRoomData folderdata = new ExpandRoomData();
            folderdata.setIsFolder(true);
            folderdata.setIsUser(false);
            folderdata.setName(folder.getName());
            topicDatas.add(folderdata);
            for (ExpandRoomData roomData : topicDataMap.get(folder.getId())) {
                topicDatas.add(roomData);
            }
        }

        boolean FirstAmongNoFolderItem = true;

        for (long key : joinTopics.keySet()) {
            TopicRoom entity = joinTopics.get(key);
            ExpandRoomData topicData = new ExpandRoomData();
            topicData.setIsFirstAmongNoFolderItem(FirstAmongNoFolderItem);
            FirstAmongNoFolderItem = false;
            topicData.setEntityId(entity.getId());
            topicData.setIsUser(false);
            topicData.setName(entity.getName());
            topicData.setIsFolder(false);
            topicData.setIsPublicTopic(entity.isPublicTopic());
            topicData.setIsStarred(entity.isStarred());
            topicDatas.add(topicData);

        }
        return topicDatas;
    }

    public List<ExpandRoomData> getRoomDatas() {
        List<ExpandRoomData> roomDatas = new ArrayList<>();

        if (isIncludeAllMember) {

            ExpandRoomData dummyData;
            if (TeamInfoLoader.getInstance().getMyLevel() != Level.Guest) {
                dummyData = new ExpandRoomData();
                dummyData.setType(JandiConstants.Entity.TYPE_EVERYWHERE);
                roomDatas.add(dummyData);
            }

            dummyData = new ExpandRoomData();
            dummyData.setType(JandiConstants.Entity.TYPE_JOINED_ROOM);
            roomDatas.add(dummyData);
        }

        List<Member> users = getUsers();
        Observable<List<ExpandRoomData>> enabledUsers = Observable.from(users)
                .filter(Member::isEnabled)
                .map(ExpandRoomData::newMemberData)
                .toSortedList(this::getCompareRooms);

        Observable<List<ExpandRoomData>> disabledUsers = Observable.from(users)
                .filter(entity -> !entity.isEnabled())
                .map(ExpandRoomData::newMemberData)
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
                .subscribe(it -> {}, Throwable::printStackTrace);

        return roomDatas;
    }

    @NonNull
    private Integer getCompareRooms(ExpandRoomData lhs, ExpandRoomData rhs) {
        if (TeamInfoLoader.getInstance().isJandiBot(lhs.getEntityId())) {
            return -1;
        } else if (TeamInfoLoader.getInstance().isJandiBot(rhs.getEntityId())) {
            return 1;
        }
        String lhsName, rhsName;
        if (!lhs.isInactive()) {
            lhsName = lhs.getName();
        } else {
            lhsName = lhs.getEmail();
        }

        if (!rhs.isInactive()) {
            rhsName = rhs.getName();
        } else {
            rhsName = rhs.getEmail();
        }

        return StringCompareUtil.compare(lhsName, rhsName);
    }

    private int getDisabledUserCount(List<Member> users) {
        return Observable.from(users)
                .filter(formattedEntity -> !formattedEntity.isEnabled())
                .count()
                .toBlocking()
                .first();
    }

    private boolean hasDisabledUser(List<Member> users) {
        return Observable.from(users)
                .filter(formattedEntity -> !formattedEntity.isEnabled())
                .map(formattedEntity1 -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

}