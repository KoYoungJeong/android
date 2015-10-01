package com.tosslab.jandi.app.ui.selector.room;

import android.view.View;

import com.tosslab.jandi.app.ui.selector.room.domain.ExpandRoomData;

public interface RoomSelector {

    void show(View roomView);

    void dismiss();

    void setType(int type);

    void setOnRoomSelectListener(OnRoomSelectListener onRoomSelectListener);

    void setOnRoomDismissListener(OnRoomDismissListener onRoomDismissListener);

    interface OnRoomSelectListener {
        void onRoomSelect(ExpandRoomData item);
    }

    interface OnRoomDismissListener {
        void onRoomDismiss();
    }

}
