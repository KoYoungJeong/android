package com.tosslab.jandi.app.ui.selector.user;


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
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.selector.room.adapter.RoomRecyclerAdapter;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;
import com.tosslab.jandi.app.utils.StringCompareUtil;

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

        RoomRecyclerAdapter adapter = new RoomRecyclerAdapter(context, RoomRecyclerAdapter.FROM_USER_SELECTOR);
        getUsers().subscribe(adapter::addAll, Throwable::printStackTrace);

        ExpandRoomData dummyData = new ExpandRoomData();
        dummyData.setType(JandiConstants.Entity.TYPE_EVERYWHERE);
        adapter.add(0, dummyData);

        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            ExpandRoomData item = adapter.getItem(position);
            if (item instanceof ExpandRoomData.DummyDisabledRoomData) {
                ExpandRoomData.DummyDisabledRoomData dummy = (ExpandRoomData.DummyDisabledRoomData) item;
                dummy.setExpanded(!dummy.isExpanded());
                adapter1.notifyDataSetChanged();
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);

            } else if (onRoomSelectListener != null) {
                onRoomSelectListener.onUserSelect(item);
            }
        });
        recyclerView.setAdapter(adapter);

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setOnDismissListener(() -> {
            if (onUserDismissListener != null) {
                onUserDismissListener.onUserDismiss();
            }
        });
        PopupWindowCompat.showAsDropDown(popupWindow, roomView, 0, 0, Gravity.TOP | Gravity.START);

    }

    @NonNull
    private List<ExpandRoomData> getDummyRoomData() {
        List<ExpandRoomData> t = new ArrayList<>();
        ExpandRoomData.DummyDisabledRoomData dummy = new ExpandRoomData.DummyDisabledRoomData(getDisabledMembers().toBlocking().firstOrDefault(new ArrayList<>()).size());
        dummy.setName(JandiApplication.getContext().getString(R.string.jandi_disabled_members));
        t.add(dummy);
        return t;
    }

    private boolean hasDisabledMembers() {
        return TeamInfoLoader.getInstance().hasDisabledUser();

    }

    private Observable<List<ExpandRoomData>> getDisabledMembers() {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(user -> !user.isEnabled())
                .map(ExpandRoomData::newMemberData)
                .toSortedList((lhs, rhs) -> {
                    return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                });

    }

    protected Observable<List<ExpandRoomData>> getUsers() {

        long myId = TeamInfoLoader.getInstance().getMyId();
        boolean guest = TeamInfoLoader.getInstance().getMyLevel() == Level.Guest;
        return Observable.defer(() -> {
            if (guest) {
                return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                        .filter(TopicRoom::isJoined)
                        .flatMap(topicRoom -> Observable.from(topicRoom.getMembers()))
                        .distinct()
                        .map(id -> TeamInfoLoader.getInstance().getUser(id));
            } else {
                return Observable.from(TeamInfoLoader.getInstance().getUserList());
            }
        })
                .filter(User::isEnabled)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .map((member) -> {
                    ExpandRoomData expandRoomData = ExpandRoomData.newMemberData(member);
                    if (member.getId() == TeamInfoLoader.getInstance().getMyId()) {
                        expandRoomData.setName(JandiApplication.getContext().getString(R.string.jandi_my_files));
                    }
                    return expandRoomData;
                })
                .toSortedList((lhs, rhs) -> {
                    if (lhs.getEntityId() == myId) {
                        return -1;
                    } else if (rhs.getEntityId() == myId) {
                        return 1;
                    } else {
                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    }
                }).concatWith(Observable.defer(() -> {
                    if (!guest && hasDisabledMembers()) {
                        return Observable.concat(Observable.just(getDummyRoomData()), getDisabledMembers());
                    } else {
                        return Observable.empty();
                    }
                }));

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

}
