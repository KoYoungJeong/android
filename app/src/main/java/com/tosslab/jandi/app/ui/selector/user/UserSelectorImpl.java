package com.tosslab.jandi.app.ui.selector.user;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.selector.room.adapter.RoomRecyclerAdapter;
import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;

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

        RoomRecyclerAdapter adapter = new RoomRecyclerAdapter(context);
        getUsers()
                .concatWith(Observable.create(subscriber -> {
                    if (hasDisabledMembers()) {
                        subscriber.onNext(getDummyRoomData());
                        getDisabledMembers()
                                .subscribe(subscriber::onNext);
                    }

                    subscriber.onCompleted();
                }))
                .subscribe(adapter::addAll);

        if (EntityManager.getInstance().hasJandiBot()) {
            adapter.add(0, ExpandRoomData.newRoomData(EntityManager.getInstance().getJandiBot()));
        }

        ExpandRoomData dummyData = new ExpandRoomData();
        dummyData.setType(FormattedEntity.TYPE_EVERYWHERE);
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
        return Observable.from(EntityManager.getInstance().getFormattedUsers())
                .filter(formattedEntity -> !TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .map(formattedEntity1 -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();

    }

    private Observable<List<ExpandRoomData>> getDisabledMembers() {
        return Observable.from(EntityManager.getInstance().getFormattedUsers())
                .filter(formattedEntity -> !TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .map(ExpandRoomData::newRoomData)
                .toSortedList((lhs, rhs) -> {
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                });

    }

    protected Observable<List<ExpandRoomData>> getUsers() {

        EntityManager entityManager = EntityManager.getInstance();
        long myId = entityManager.getMe().getId();
        return Observable.from(entityManager.getFormattedUsers())
                .filter(formattedEntity -> TextUtils.equals(formattedEntity.getUser().status, "enabled"))
                .map(ExpandRoomData::newRoomData)
                .toSortedList((lhs, rhs) -> {
                    if (lhs.getEntityId() == myId) {
                        return -1;
                    } else if (rhs.getEntityId() == myId) {
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

}
